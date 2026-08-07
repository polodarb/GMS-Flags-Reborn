package ua.polodarb.gmsflags.data.phenotype.root.diagnostic

import android.content.pm.PackageManager
import java.io.File
import java.io.RandomAccessFile
import ua.polodarb.xposed.info.XposedConstants

/**
 * Reads the Xposed module's log files from a target app's data dir. The module writes them into
 * `<targetApp.dataDir>/<XposedConstants.XPOSED_DIR>/logs/` as `<name>_xposed.log` files (inside the
 * TARGET app's data dir, so cross-uid - this runs in the root service). Files are concatenated
 * newest-first and the
 * total is capped to stay well under the binder transaction limit. Never throws: any failure (no
 * app, no dir, no logs, unreadable file) yields an empty string.
 */
internal class XposedLogsReader(
    private val packageManager: PackageManager,
) {
    fun read(androidPackageName: String): String {
        val dataDirectory = runCatching {
            packageManager.getApplicationInfo(androidPackageName, 0).dataDir
        }.getOrNull() ?: return ""
        val logsDirectory = File(File(dataDirectory, XposedConstants.XPOSED_DIR), LOGS_DIR)
        val logFiles = runCatching {
            logsDirectory.listFiles { file ->
                file.isFile && file.name.endsWith(LOG_FILE_SUFFIX)
            }
        }.getOrNull()?.sortedByDescending(File::lastModified).orEmpty()
        if (logFiles.isEmpty()) return ""

        val builder = StringBuilder()
        var remaining = MAX_TOTAL_BYTES
        for (file in logFiles) {
            if (remaining <= 0) break
            val text = runCatching { file.readTail(remaining) }.getOrNull()
            if (text.isNullOrEmpty()) continue
            if (builder.isNotEmpty()) builder.append('\n')
            builder.append("===== ").append(file.name).append(" =====\n").append(text)
            remaining -= text.toByteArray(Charsets.UTF_8).size
        }
        return builder.toString()
    }

    private fun File.readTail(limitBytes: Int): String {
        val length = length()
        if (length <= 0L) return ""
        RandomAccessFile(this, "r").use { access ->
            val start = if (length > limitBytes) length - limitBytes else 0L
            access.seek(start)
            val bytes = ByteArray((length - start).toInt())
            access.readFully(bytes)
            return String(bytes, Charsets.UTF_8)
        }
    }

    private companion object {
        const val LOGS_DIR = "logs"
        const val LOG_FILE_SUFFIX = "_xposed.log"
        const val MAX_TOTAL_BYTES = 256 * 1024
    }
}
