package ua.polodarb.gmsflags.data.phenotype.root.hooks

import android.content.pm.PackageManager
import io.requery.android.database.sqlite.SQLiteDatabase
import java.io.File
import ua.polodarb.gmsflags.data.phenotype.root.diagnostic.PairipCoreDetector
import ua.polodarb.gmsflags.data.phenotype.root.parcel.HookDiagnosticSnapshotParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.HookStrategyDiagnosticParcel
import ua.polodarb.xposed.info.HookDiagnosticContract
import ua.polodarb.xposed.info.XposedConstants

internal class HookDiagnosticsReader(
    private val packageManager: PackageManager,
    private val pairipCoreDetector: PairipCoreDetector = PairipCoreDetector(),
) {
    fun read(androidPackageNames: List<String>): List<HookDiagnosticSnapshotParcel> =
        androidPackageNames.distinct().mapNotNull(::readTarget)

    private fun readTarget(androidPackageName: String): HookDiagnosticSnapshotParcel? {
        val applicationInfo = runCatching {
            packageManager.getApplicationInfo(androidPackageName, 0)
        }.getOrNull() ?: return null
        val dataDirectory = applicationInfo.dataDir
        val compatibilityWarnings = buildList {
            if (pairipCoreDetector.isPresent(applicationInfo)) {
                add(HookDiagnosticContract.COMPATIBILITY_WARNING_PAIRIP_CORE)
            }
        }
        val runtimeDirectory = File(dataDirectory, XposedConstants.XPOSED_DIR)
        val overrideCount = readOverrideCount(
            File(runtimeDirectory, XposedConstants.RUNTIME_OVERRIDES_DB_FILE_NAME)
        )
        val diagnosticsFile = File(
            runtimeDirectory,
            XposedConstants.HOOK_DIAGNOSTICS_DB_FILE_NAME,
        )
        val session = readLatestSession(
            file = diagnosticsFile,
            mainProcessName = androidPackageName,
        )
        return if (session == null) {
            HookDiagnosticSnapshotParcel(
                androidPackageName = androidPackageName,
                currentOverrideCount = overrideCount,
                hasSession = false,
                processName = "",
                versionCode = 0,
                startedAt = 0,
                updatedAt = 0,
                loadedOverrideCount = 0,
                state = "",
                error = null,
                strategies = emptyList(),
                compatibilityWarnings = compatibilityWarnings,
            )
        } else {
            session.copy(
                androidPackageName = androidPackageName,
                currentOverrideCount = overrideCount,
                strategies = readStrategies(diagnosticsFile, session.sessionId),
            ).toParcel(compatibilityWarnings)
        }
    }

    private fun readOverrideCount(file: File): Int = readDatabase(file) { database ->
        database.rawQuery(
            "SELECT COUNT(*) FROM ${XposedConstants.RUNTIME_OVERRIDES_TABLE}",
            null,
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
    } ?: 0

    private fun readLatestSession(
        file: File,
        mainProcessName: String,
    ): SessionRow? = readDatabase(file) { database ->
        database.rawQuery(
            """
            SELECT session_id, process_name, version_code, started_at, updated_at,
                   override_count, state, error
            FROM ${XposedConstants.HOOK_DIAGNOSTICS_SESSION_TABLE}
            ORDER BY CASE WHEN process_name = ? THEN 0 ELSE 1 END, started_at DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(mainProcessName),
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            SessionRow(
                sessionId = cursor.getString(0),
                processName = cursor.getString(1),
                versionCode = cursor.getLong(2),
                startedAt = cursor.getLong(3),
                updatedAt = cursor.getLong(4),
                loadedOverrideCount = cursor.getInt(5),
                state = cursor.getString(6),
                error = cursor.getString(7),
            )
        }
    }

    private fun readStrategies(file: File, sessionId: String): List<HookStrategyDiagnosticParcel> =
        readDatabase(file) { database ->
            database.rawQuery(
                """
                SELECT strategy, state, applied_count, consumed_count, message
                FROM ${XposedConstants.HOOK_DIAGNOSTICS_STRATEGY_TABLE}
                WHERE session_id = ?
                ORDER BY strategy ASC
                """.trimIndent(),
                arrayOf(sessionId),
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            HookStrategyDiagnosticParcel(
                                name = cursor.getString(0),
                                state = cursor.getString(1),
                                appliedCount = cursor.getInt(2),
                                consumedCount = cursor.getInt(3),
                                message = cursor.getString(4),
                            )
                        )
                    }
                }
            }
        }.orEmpty()

    private fun <T> readDatabase(file: File, block: (SQLiteDatabase) -> T): T? {
        if (!file.isFile) return null
        return runCatching {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use(block)
        }.getOrNull()
    }

    private data class SessionRow(
        val sessionId: String,
        val androidPackageName: String = "",
        val currentOverrideCount: Int = 0,
        val processName: String,
        val versionCode: Long,
        val startedAt: Long,
        val updatedAt: Long,
        val loadedOverrideCount: Int,
        val state: String,
        val error: String?,
        val strategies: List<HookStrategyDiagnosticParcel> = emptyList(),
    ) {
        fun toParcel(compatibilityWarnings: List<String>) = HookDiagnosticSnapshotParcel(
            androidPackageName = androidPackageName,
            currentOverrideCount = currentOverrideCount,
            hasSession = true,
            processName = processName,
            versionCode = versionCode,
            startedAt = startedAt,
            updatedAt = updatedAt,
            loadedOverrideCount = loadedOverrideCount,
            state = state,
            error = error,
            strategies = strategies,
            compatibilityWarnings = compatibilityWarnings,
        )
    }
}
