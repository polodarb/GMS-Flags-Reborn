package ua.polodarb.gmsflags.data.phenotype.sqlite

import io.requery.android.database.sqlite.SQLiteDatabase

internal class SqlitePhenotypeFlagDatabaseReader(
    private val path: String = PhenotypeDatabasePaths.GMS,
) : PhenotypeFlagDatabaseReader {
    override fun readFlags(phenotypePackageName: String): List<StoredPhenotypeFlag> {
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY).use { database ->
            if (database.hasTable("param_partitions")) {
                readModernFlags(database, phenotypePackageName)
            } else {
                readLegacyFlags(database, phenotypePackageName)
            }
        }
    }

    private fun readModernFlags(
        database: SQLiteDatabase,
        phenotypePackageName: String,
    ): List<StoredPhenotypeFlag> {
        val flagsByIdentity = linkedMapOf<Pair<Int, String>, StoredPhenotypeFlag>()
        database.rawQuery(
            MODERN_FLAGS_QUERY,
            arrayOf(phenotypePackageName.substringBefore('#')),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                runCatching { PhixitFlagsCodec.decode(cursor.getBlob(0)) }
                    .getOrDefault(emptyList())
                    .mapNotNull { it.toStoredFlagOrNull() }
                    .forEach { flag -> flagsByIdentity[flag.type to flag.name] = flag }
            }
        }
        return flagsByIdentity.values.toList()
    }

    private fun readLegacyFlags(
        database: SQLiteDatabase,
        phenotypePackageName: String,
    ): List<StoredPhenotypeFlag> = buildList {
        database.rawQuery(LEGACY_FLAGS_QUERY, arrayOf(phenotypePackageName)).use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)?.takeIf(String::isNotBlank) ?: continue
                when {
                    !cursor.isNull(1) -> add(StoredPhenotypeFlag(name, TYPE_BOOLEAN, cursor.getString(1)))
                    !cursor.isNull(2) -> add(StoredPhenotypeFlag(name, TYPE_INTEGER, cursor.getString(2)))
                    !cursor.isNull(3) -> add(StoredPhenotypeFlag(name, TYPE_FLOAT, cursor.getString(3)))
                    !cursor.isNull(4) -> add(StoredPhenotypeFlag(name, TYPE_STRING, cursor.getString(4)))
                }
            }
        }
    }.distinctBy { it.type to it.name }

    private fun SQLiteDatabase.hasTable(name: String): Boolean = rawQuery(
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
        arrayOf(name),
    ).use { it.moveToFirst() }

    private fun PhixitFlag.toStoredFlagOrNull(): StoredPhenotypeFlag? = when (this) {
        is PhixitFlag.Bool -> StoredPhenotypeFlag(name, TYPE_BOOLEAN, if (value) "1" else "0")
        is PhixitFlag.Int -> StoredPhenotypeFlag(name, TYPE_INTEGER, value.toString())
        is PhixitFlag.Float -> StoredPhenotypeFlag(
            name,
            TYPE_FLOAT,
            java.lang.Double.longBitsToDouble(value).toString(),
        )
        is PhixitFlag.StringValue -> StoredPhenotypeFlag(name, TYPE_STRING, value)
        is PhixitFlag.Extension -> null
    }

    private companion object {
        const val TYPE_BOOLEAN = 0
        const val TYPE_INTEGER = 1
        const val TYPE_FLOAT = 2
        const val TYPE_STRING = 3
        const val MODERN_FLAGS_QUERY = """
            SELECT pp.flags_content
            FROM param_partitions pp
            WHERE pp.static_config_package_id = (
                SELECT static_config_package_id
                FROM static_config_packages
                WHERE name = ?
                ORDER BY rowid DESC, static_config_package_id DESC
                LIMIT 1
            )
            ORDER BY pp.param_partition_id ASC
        """

        const val LEGACY_FLAGS_QUERY = """
            SELECT name, boolVal, intVal, floatVal, stringVal
            FROM Flags
            WHERE packageName = ?
            ORDER BY name ASC
        """
    }
}
