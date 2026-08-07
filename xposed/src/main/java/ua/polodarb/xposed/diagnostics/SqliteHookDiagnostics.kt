package ua.polodarb.xposed.diagnostics

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Process
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import ua.polodarb.xposed.info.HookDiagnosticContract
import ua.polodarb.xposed.info.XposedConstants
import ua.polodarb.xposed.logging.XposedLogger

internal class SqliteHookDiagnostics(
    private val databaseFile: File,
    private val packageName: String,
    private val processName: String,
    private val versionCode: Long,
    private val clock: () -> Long = System::currentTimeMillis,
) : HookDiagnostics {
    private val sessionId = "${Process.myPid()}-${clock()}-${UUID.randomUUID()}"
    private val strategies = ConcurrentHashMap<String, StrategySnapshot>()
    private val appliedIdentities = ConcurrentHashMap.newKeySet<String>()
    private val consumedIdentities = ConcurrentHashMap.newKeySet<String>()
    private val dirty = AtomicBoolean(false)
    private val writer = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "gmsflags-diagnostics").apply { isDaemon = true }
    }

    @Volatile private var state = HookDiagnosticContract.STATE_STARTED
    @Volatile private var error: String? = null
    @Volatile private var overrideCount = 0

    override fun start(overrideCount: Int) {
        this.overrideCount = overrideCount
        dirty.set(true)
        flushSafely()
        writer.scheduleWithFixedDelay(::flushSafely, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS)
    }

    override fun noOverrides() {
        state = HookDiagnosticContract.STATE_NO_OVERRIDES
        dirty.set(true)
        flushSafely()
    }

    override fun paused() {
        state = HookDiagnosticContract.STATE_PAUSED
        dirty.set(true)
        flushSafely()
    }

    override fun strategyInstalled(strategy: String) {
        strategies.compute(strategy) { _, current ->
            (current ?: StrategySnapshot()).copy(state = HookDiagnosticContract.STRATEGY_STATE_INSTALLED)
        }
        state = HookDiagnosticContract.STATE_INSTALLED
        dirty.set(true)
    }

    override fun strategyUnavailable(strategy: String, reason: String) {
        strategies.compute(strategy) { _, current ->
            (current ?: StrategySnapshot()).copy(
                state = HookDiagnosticContract.STRATEGY_STATE_UNAVAILABLE,
                message = reason,
            )
        }
        dirty.set(true)
    }

    override fun strategyFailed(strategy: String, reason: String) {
        strategies.compute(strategy) { _, current ->
            (current ?: StrategySnapshot()).copy(
                state = HookDiagnosticContract.STRATEGY_STATE_FAILED,
                message = reason.take(MAX_MESSAGE_LENGTH),
            )
        }
        dirty.set(true)
    }

    override fun overrideApplied(strategy: String, identity: String) {
        if (!appliedIdentities.add("$strategy:$identity")) return
        strategies.compute(strategy) { _, current ->
            val snapshot = current ?: StrategySnapshot()
            snapshot.copy(appliedCount = snapshot.appliedCount + 1)
        }
        dirty.set(true)
    }

    override fun overrideConsumed(strategy: String, identity: String) {
        if (!consumedIdentities.add("$strategy:$identity")) return
        strategies.compute(strategy) { _, current ->
            val snapshot = current ?: StrategySnapshot()
            snapshot.copy(consumedCount = snapshot.consumedCount + 1)
        }
        dirty.set(true)
    }

    override fun failure(message: String) {
        state = HookDiagnosticContract.STATE_FAILED
        error = message.take(MAX_MESSAGE_LENGTH)
        dirty.set(true)
        flushSafely()
    }

    private fun flushSafely() {
        runCatching(::flush).onFailure { failure ->
            XposedLogger.logW("Failed to persist hook diagnostics: ${failure.message}")
        }
    }

    @Synchronized
    private fun flush() {
        if (!dirty.compareAndSet(true, false)) return
        databaseFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { database ->
            database.execSQL(CREATE_SESSION_TABLE)
            database.execSQL(CREATE_STRATEGY_TABLE)
            database.beginTransaction()
            try {
                val now = clock()
                database.insertWithOnConflict(
                    XposedConstants.HOOK_DIAGNOSTICS_SESSION_TABLE,
                    null,
                    ContentValues().apply {
                        put(COLUMN_SESSION_ID, sessionId)
                        put(COLUMN_PACKAGE_NAME, packageName)
                        put(COLUMN_PROCESS_NAME, processName)
                        put(COLUMN_PID, Process.myPid())
                        put(COLUMN_VERSION_CODE, versionCode)
                        put(COLUMN_STARTED_AT, sessionId.substringAfter('-').substringBefore('-').toLongOrNull() ?: now)
                        put(COLUMN_UPDATED_AT, now)
                        put(COLUMN_OVERRIDE_COUNT, overrideCount)
                        put(COLUMN_STATE, state)
                        put(COLUMN_ERROR, error)
                    },
                    SQLiteDatabase.CONFLICT_REPLACE,
                )
                strategies.forEach { (strategy, snapshot) ->
                    database.insertWithOnConflict(
                        XposedConstants.HOOK_DIAGNOSTICS_STRATEGY_TABLE,
                        null,
                        ContentValues().apply {
                            put(COLUMN_SESSION_ID, sessionId)
                            put(COLUMN_STRATEGY, strategy)
                            put(COLUMN_STATE, snapshot.state)
                            put(COLUMN_APPLIED_COUNT, snapshot.appliedCount)
                            put(COLUMN_CONSUMED_COUNT, snapshot.consumedCount)
                            put(COLUMN_MESSAGE, snapshot.message)
                            put(COLUMN_UPDATED_AT, now)
                        },
                        SQLiteDatabase.CONFLICT_REPLACE,
                    )
                }
                prune(database)
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
        }
    }

    private fun prune(database: SQLiteDatabase) {
        database.execSQL(
            """
            DELETE FROM ${XposedConstants.HOOK_DIAGNOSTICS_SESSION_TABLE}
            WHERE $COLUMN_SESSION_ID NOT IN (
                SELECT $COLUMN_SESSION_ID
                FROM ${XposedConstants.HOOK_DIAGNOSTICS_SESSION_TABLE}
                ORDER BY $COLUMN_STARTED_AT DESC
                LIMIT $MAX_SESSIONS
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            DELETE FROM ${XposedConstants.HOOK_DIAGNOSTICS_STRATEGY_TABLE}
            WHERE $COLUMN_SESSION_ID NOT IN (
                SELECT $COLUMN_SESSION_ID FROM ${XposedConstants.HOOK_DIAGNOSTICS_SESSION_TABLE}
            )
            """.trimIndent()
        )
    }

    private data class StrategySnapshot(
        val state: String = HookDiagnosticContract.STRATEGY_STATE_PENDING,
        val appliedCount: Int = 0,
        val consumedCount: Int = 0,
        val message: String? = null,
    )

    private companion object {
        const val FLUSH_INTERVAL_MS = 600L
        const val MAX_SESSIONS = 8
        const val MAX_MESSAGE_LENGTH = 500
        const val COLUMN_SESSION_ID = "session_id"
        const val COLUMN_PACKAGE_NAME = "package_name"
        const val COLUMN_PROCESS_NAME = "process_name"
        const val COLUMN_PID = "pid"
        const val COLUMN_VERSION_CODE = "version_code"
        const val COLUMN_STARTED_AT = "started_at"
        const val COLUMN_UPDATED_AT = "updated_at"
        const val COLUMN_OVERRIDE_COUNT = "override_count"
        const val COLUMN_STATE = "state"
        const val COLUMN_ERROR = "error"
        const val COLUMN_STRATEGY = "strategy"
        const val COLUMN_APPLIED_COUNT = "applied_count"
        const val COLUMN_CONSUMED_COUNT = "consumed_count"
        const val COLUMN_MESSAGE = "message"

        const val CREATE_SESSION_TABLE = """
            CREATE TABLE IF NOT EXISTS ${XposedConstants.HOOK_DIAGNOSTICS_SESSION_TABLE} (
                session_id TEXT PRIMARY KEY,
                package_name TEXT NOT NULL,
                process_name TEXT NOT NULL,
                pid INTEGER NOT NULL,
                version_code INTEGER NOT NULL,
                started_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                override_count INTEGER NOT NULL,
                state TEXT NOT NULL,
                error TEXT
            )
        """
        const val CREATE_STRATEGY_TABLE = """
            CREATE TABLE IF NOT EXISTS ${XposedConstants.HOOK_DIAGNOSTICS_STRATEGY_TABLE} (
                session_id TEXT NOT NULL,
                strategy TEXT NOT NULL,
                state TEXT NOT NULL,
                applied_count INTEGER NOT NULL,
                consumed_count INTEGER NOT NULL,
                message TEXT,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(session_id, strategy)
            )
        """
    }
}
