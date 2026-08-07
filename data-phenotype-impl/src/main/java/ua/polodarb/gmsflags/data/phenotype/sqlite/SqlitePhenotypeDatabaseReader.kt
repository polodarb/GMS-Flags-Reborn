package ua.polodarb.gmsflags.data.phenotype.sqlite

import io.requery.android.database.sqlite.SQLiteDatabase
import ua.polodarb.xposed.info.XposedTargets

class SqlitePhenotypeDatabaseReader(
    private val path: String = PhenotypeDatabasePaths.GMS,
) : PhenotypeDatabaseReader {
    override fun readPhenotypePackages(): List<PhenotypePackageBindingRecord> {
        val database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
        return database.use { db ->
            readModernBindings(db).ifEmpty { readLegacyBindings(db) }
        }
    }

    private fun readModernBindings(database: SQLiteDatabase): List<PhenotypePackageBindingRecord> = runCatching {
        database.rawQuery(MODERN_BINDINGS_QUERY, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val phenotypePackage = cursor.getString(0)?.takeIf(String::isNotBlank) ?: continue
                    val androidPackage = cursor.getString(1)?.takeIf(String::isNotBlank)
                        ?: XposedTargets.runtimeTargetPackageForFlagPackage(phenotypePackage)
                    add(PhenotypePackageBindingRecord(phenotypePackage, androidPackage))
                }
            }
        }
    }.getOrDefault(emptyList())

    private fun readLegacyBindings(database: SQLiteDatabase): List<PhenotypePackageBindingRecord> = runCatching {
        database.rawQuery(LEGACY_BINDINGS_QUERY, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val phenotypePackage = cursor.getString(0)?.takeIf(String::isNotBlank) ?: continue
                    val mappedPackage = cursor.getString(1)?.takeIf(String::isNotBlank)
                    add(
                        PhenotypePackageBindingRecord(
                            phenotypePackageName = phenotypePackage,
                            androidPackageName = mappedPackage
                                ?: XposedTargets.runtimeTargetPackageForFlagPackage(phenotypePackage),
                        )
                    )
                }
            }
        }
    }.getOrDefault(emptyList())

    private companion object {
        const val MODERN_BINDINGS_QUERY = """
            SELECT DISTINCT cp.name, ap.name
            FROM config_packages cp
            JOIN android_packages ap
                ON ap.android_package_id = cp.android_package_id
            WHERE cp.name IS NOT NULL AND ap.name IS NOT NULL
        """

        const val LEGACY_BINDINGS_QUERY = """
            SELECT DISTINCT f.packageName, p.androidPackageName
            FROM Flags f
            LEFT JOIN Packages p ON p.packageName = f.packageName
            WHERE f.packageName IS NOT NULL
        """
    }
}
