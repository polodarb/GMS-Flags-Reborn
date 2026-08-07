package ua.polodarb.gmsflags.data.phenotype.root.diagnostic

import io.requery.android.database.sqlite.SQLiteDatabase
import java.io.File
import ua.polodarb.gmsflags.data.phenotype.root.parcel.XposedScopeSnapshotParcel

internal class LsposedScopeReader(
    private val databasePaths: List<String> = DEFAULT_DATABASE_PATHS,
) {
    fun read(modulePackageName: String, userId: Int): XposedScopeSnapshotParcel {
        require(modulePackageName.isNotBlank()) { "Module package name cannot be blank" }
        return databasePaths.asSequence()
            .map(::File)
            .filter(File::isFile)
            .mapNotNull { file -> runCatching { readDatabase(file, modulePackageName, userId) }.getOrNull() }
            .firstOrNull()
            ?: error("LSPosed scope could not be read")
    }

    private fun readDatabase(
        databaseFile: File,
        modulePackageName: String,
        userId: Int,
    ): XposedScopeSnapshotParcel = SQLiteDatabase.openDatabase(
        databaseFile.path,
        null,
        SQLiteDatabase.OPEN_READONLY,
    ).use { database ->
        val module = database.rawQuery(
            "SELECT mid, enabled FROM modules WHERE module_pkg_name = ? LIMIT 1",
            arrayOf(modulePackageName),
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                ModuleRow(id = cursor.getLong(0), enabled = cursor.getInt(1) == 1)
            } else {
                null
            }
        }

        if (module == null) {
            XposedScopeSnapshotParcel(
                moduleFound = false,
                moduleEnabled = false,
                scopedPackageNames = emptyList(),
            )
        } else {
            val packages = database.rawQuery(
                "SELECT app_pkg_name FROM scope WHERE mid = ? AND user_id = ?",
                arrayOf(module.id.toString(), userId.toString()),
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(0))
                    }
                }
            }
            XposedScopeSnapshotParcel(
                moduleFound = true,
                moduleEnabled = module.enabled,
                scopedPackageNames = packages.distinct().sorted(),
            )
        }
    }

    private data class ModuleRow(val id: Long, val enabled: Boolean)

    private companion object {
        val DEFAULT_DATABASE_PATHS = listOf(
            "/data/adb/lspd/config/modules_config.db",
            "/data/adb/lspd/config/modules_config_snapshot.db",
        )
    }
}
