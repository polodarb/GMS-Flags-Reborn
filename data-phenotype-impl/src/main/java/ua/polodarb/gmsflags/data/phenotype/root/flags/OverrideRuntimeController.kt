package ua.polodarb.gmsflags.data.phenotype.root.flags

import android.content.pm.PackageManager
import android.system.Os
import io.requery.android.database.sqlite.SQLiteDatabase
import java.io.File
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeFlagOverrideStore
import ua.polodarb.xposed.info.XposedConstants

internal class OverrideRuntimeController(
    private val packageManager: PackageManager,
    private val overrideStore: RuntimeFlagOverrideStore,
) {
    fun readOverrideCount(androidPackageNames: List<String>): Int =
        targets(androidPackageNames).sumOf { target ->
            val databaseFile = File(target.runtimeDirectory, XposedConstants.RUNTIME_OVERRIDES_DB_FILE_NAME)
            if (!databaseFile.isFile) return@sumOf 0
            runCatching {
                SQLiteDatabase.openDatabase(
                    databaseFile.path,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                ).use { database ->
                    database.rawQuery(
                        "SELECT COUNT(*) FROM ${XposedConstants.RUNTIME_OVERRIDES_TABLE}",
                        null,
                    ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
                }
            }.getOrDefault(0)
        }

    fun readPaused(androidPackageNames: List<String>): Boolean {
        val targets = targets(androidPackageNames)
        return targets.isNotEmpty() && targets.all { target ->
            File(target.runtimeDirectory, XposedConstants.OVERRIDES_PAUSED_FILE_NAME).isFile
        }
    }

    fun setPaused(androidPackageNames: List<String>, paused: Boolean) {
        targets(androidPackageNames).forEach { target ->
            val marker = File(target.runtimeDirectory, XposedConstants.OVERRIDES_PAUSED_FILE_NAME)
            if (paused) {
                target.runtimeDirectory.mkdirs()
                marker.writeText("paused")
                Os.chown(target.runtimeDirectory.path, target.uid, target.uid)
                Os.chmod(target.runtimeDirectory.path, MODE_OWNER_DIRECTORY)
                Os.chown(marker.path, target.uid, target.uid)
                Os.chmod(marker.path, MODE_OWNER_FILE)
                restoreContext(target.runtimeDirectory, marker)
            } else {
                marker.delete()
            }
        }
    }

    fun deleteAll(androidPackageNames: List<String>) {
        targets(androidPackageNames).forEach { target ->
            overrideStore.deleteAll(target.packageName)
        }
    }

    private fun targets(androidPackageNames: List<String>): List<Target> = androidPackageNames
        .distinct()
        .mapNotNull { packageName ->
            runCatching {
                val info = packageManager.getApplicationInfo(packageName, 0)
                Target(
                    packageName = packageName,
                    uid = info.uid,
                    runtimeDirectory = File(info.dataDir, XposedConstants.XPOSED_DIR),
                )
            }.getOrNull()
        }

    private fun restoreContext(directory: File, marker: File) {
        val result = ProcessBuilder("restorecon", "-F", directory.path, marker.path)
            .redirectErrorStream(true)
            .start()
            .waitFor()
        check(result == 0) { "Unable to restore SELinux context for override pause state" }
    }

    private data class Target(
        val packageName: String,
        val uid: Int,
        val runtimeDirectory: File,
    )

    private companion object {
        const val MODE_OWNER_DIRECTORY = 448
        const val MODE_OWNER_FILE = 384
    }
}
