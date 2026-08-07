package ua.polodarb.gmsflags.data.repository.report.datasource

/**
 * Reads the Xposed module's log files from a target app's data dir (cross-uid, so it goes through
 * the root service). Returns the concatenated log text, or an empty string when there are no logs.
 * Mirrors [ua.polodarb.gmsflags.data.repository.hookstatus.HookDiagnosticsDataSource].
 */
fun interface XposedLogsDataSource {
    suspend fun read(androidPackageName: String): Result<String>
}
