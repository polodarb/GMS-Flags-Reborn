package ua.polodarb.xposed.logging

import android.os.StrictMode
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object XposedLogger {

    private const val TAG = "GmsFlagsXposedEntry"
    private const val LOGS_DIR = "logs"
    private const val LOG_FILE_SUFFIX = "_xposed.log"

    private const val MAX_LOG_FILES = 8
    private const val MAX_LOG_DIR_BYTES = 3L * 1024 * 1024 // ~3 MB across all files
    private const val MAX_LOG_FILE_BYTES = 512L * 1024 // ~512 KB per single file (incl. the active one)

    private val LOG_START_TIME = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    private val LOG_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val LOG_LOCK = Any()
    private val PENDING_LOGS = mutableListOf<String>()

    private var debugLogFile: File? = null

    val logFilePath: String? get() = debugLogFile?.absolutePath

    fun initFileLogging(xposedDir: File, lpparam: XC_LoadPackage.LoadPackageParam) {
        val logDir = File(xposedDir, LOGS_DIR)
        if (!logDir.exists() && !logDir.mkdirs()) {
            logE("Failed to create debug log directory: ${logDir.path}")
            return
        }

        pruneOldLogs(logDir)

        val logFile = File(logDir, "$LOG_START_TIME$LOG_FILE_SUFFIX")
        val tz = TimeZone.getDefault()
        val now = Date()
        val offsetStr = SimpleDateFormat("XXX", Locale.US).apply { timeZone = tz }.format(now)
        val prettyTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(now)
        val header = "=== Log started at $prettyTime UTC ($offsetStr) — ${tz.id} ==="

        val oldPolicy = StrictMode.allowThreadDiskWrites()
        try {
            logFile.appendText("$header\n")
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }

        synchronized(LOG_LOCK) {
            debugLogFile = logFile
        }
        logI("Debug file logging enabled: ${logFile.path} for ${lpparam.packageName} (${lpparam.processName})")
    }

    private fun pruneOldLogs(logDir: File) {
        val oldPolicy = StrictMode.allowThreadDiskWrites()
        try {
            val files = logDir.listFiles { file -> file.isFile && file.name.endsWith(LOG_FILE_SUFFIX) }
                ?.sortedByDescending { it.lastModified() }
                ?: return
            var keptCount = 0
            var keptBytes = 0L
            for (file in files) {
                if (keptCount >= MAX_LOG_FILES || keptBytes >= MAX_LOG_DIR_BYTES) {
                    runCatching { file.delete() }
                } else {
                    keptCount++
                    keptBytes += file.length()
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to prune old logs", t)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    fun logD(message: String) {
        Log.d(TAG, message)
        writeDebugLog("D", message)
    }

    fun logI(message: String) {
        Log.i(TAG, message)
        writeDebugLog("I", message)
    }

    fun logW(message: String) {
        Log.w(TAG, message)
        writeDebugLog("W", message)
    }

    fun logE(message: String, throwable: Throwable? = null) {
        if (throwable == null) {
            Log.e(TAG, message)
        } else {
            Log.e(TAG, message, throwable)
        }
        writeDebugLog("E", message, throwable)
    }

    private fun writeDebugLog(level: String, message: String, throwable: Throwable? = null) {
        synchronized(LOG_LOCK) {
            val line = buildString {
                append(LOG_DATE_FORMAT.format(Date()))
                append(' ')
                append(level)
                append(' ')
                append('[')
                append(Thread.currentThread().name)
                append("] ")
                append(message)
                if (throwable != null) {
                    append('\n')
                    append(Log.getStackTraceString(throwable))
                }
            }

            val file = debugLogFile
            if (file == null) {
                PENDING_LOGS += line
                return
            }

            val oldPolicy = StrictMode.allowThreadDiskWrites()
            try {
                if (file.length() >= MAX_LOG_FILE_BYTES) {
                    file.writeText(
                        "=== Log truncated at ${LOG_DATE_FORMAT.format(Date())} " +
                            "(exceeded ${MAX_LOG_FILE_BYTES / 1024} KB) ===\n",
                    )
                }
                if (PENDING_LOGS.isNotEmpty()) {
                    file.appendText(PENDING_LOGS.joinToString(separator = "\n", postfix = "\n"))
                    PENDING_LOGS.clear()
                }
                file.appendText("$line\n")
            } finally {
                StrictMode.setThreadPolicy(oldPolicy)
            }
        }
    }
}
