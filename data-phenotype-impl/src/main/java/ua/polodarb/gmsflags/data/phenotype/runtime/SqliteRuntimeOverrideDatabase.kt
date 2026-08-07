package ua.polodarb.gmsflags.data.phenotype.runtime

import android.content.ContentValues
import io.requery.android.database.sqlite.SQLiteDatabase
import java.io.File
import ua.polodarb.xposed.info.XposedConstants

internal class SqliteRuntimeOverrideDatabase : RuntimeOverrideDatabase {
    override fun read(
        file: File,
        phenotypePackageName: String,
    ): List<RuntimeFlagOverride> {
        if (!file.isFile) return emptyList()
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            if (!db.hasOverrideTable()) return@use emptyList()
            db.rawQuery(READ_SQL, arrayOf(phenotypePackageName)).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            RuntimeFlagOverride(
                                packageName = cursor.getString(0),
                                name = cursor.getString(1),
                                type = cursor.getInt(2),
                                value = cursor.getString(3),
                            )
                        )
                    }
                }
            }
        }
    }

    override fun write(
        file: File,
        phenotypePackageName: String,
        overrides: List<RuntimeFlagOverride>,
    ) {
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL(CREATE_TABLE_SQL)
            db.beginTransaction()
            try {
                overrides.forEach { override ->
                    db.insertWithOnConflict(
                        XposedConstants.RUNTIME_OVERRIDES_TABLE,
                        null,
                        ContentValues().apply {
                            put(COLUMN_PACKAGE, phenotypePackageName)
                            put(COLUMN_NAME, override.name)
                            put(COLUMN_TYPE, override.type)
                            put(COLUMN_VALUE, override.value)
                        },
                        SQLiteDatabase.CONFLICT_REPLACE,
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    override fun writeMicroHooks(
        file: File,
        androidPackageName: String,
        hooks: List<RuntimeMicroHookOverride>,
    ) {
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL(CREATE_MICRO_HOOKS_TABLE_SQL)
            db.beginTransaction()
            try {
                hooks.forEach { hook ->
                    db.insertWithOnConflict(
                        XposedConstants.RUNTIME_MICRO_HOOKS_TABLE,
                        null,
                        ContentValues().apply {
                            put(MICRO_HOOK_COLUMN_PACKAGE, androidPackageName)
                            put(MICRO_HOOK_COLUMN_RECIPE_ID, hook.recipeId)
                            put(MICRO_HOOK_COLUMN_PAYLOAD_BASE64, hook.payloadBase64)
                            put(MICRO_HOOK_COLUMN_PAYLOAD_SHA256, hook.payloadSha256)
                            put(MICRO_HOOK_COLUMN_SIGNATURE_BASE64, hook.signatureBase64)
                            put(MICRO_HOOK_COLUMN_REQUIRED, if (hook.required) 1 else 0)
                        },
                        SQLiteDatabase.CONFLICT_REPLACE,
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    override fun delete(
        file: File,
        phenotypePackageName: String,
        flagName: String,
    ): Boolean = editExisting(file) { db ->
        db.delete(
            XposedConstants.RUNTIME_OVERRIDES_TABLE,
            "$COLUMN_PACKAGE = ? AND $COLUMN_NAME = ?",
            arrayOf(phenotypePackageName, flagName),
        )
    }

    override fun delete(
        file: File,
        phenotypePackageName: String,
        flagNames: List<String>,
    ): Boolean {
        if (flagNames.isEmpty()) return false
        return editExisting(file) { db ->
            db.beginTransaction()
            try {
                flagNames.distinct().chunked(MAX_DELETE_BATCH_SIZE).forEach { names ->
                    val placeholders = List(names.size) { "?" }.joinToString(",")
                    db.delete(
                        XposedConstants.RUNTIME_OVERRIDES_TABLE,
                        "$COLUMN_PACKAGE = ? AND $COLUMN_NAME IN ($placeholders)",
                        arrayOf(phenotypePackageName, *names.toTypedArray()),
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    override fun deletePackage(file: File, phenotypePackageName: String): Boolean =
        editExisting(file) { db ->
            db.delete(
                XposedConstants.RUNTIME_OVERRIDES_TABLE,
                "$COLUMN_PACKAGE = ?",
                arrayOf(phenotypePackageName),
            )
        }

    override fun deleteMicroHooks(
        file: File,
        androidPackageName: String,
        recipeIds: List<Long>,
    ): Boolean {
        if (recipeIds.isEmpty()) return false
        if (!file.isFile) return false
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            if (!db.hasMicroHooksTable()) return@use false
            db.beginTransaction()
            try {
                recipeIds.distinct().chunked(MAX_DELETE_BATCH_SIZE).forEach { ids ->
                    val placeholders = List(ids.size) { "?" }.joinToString(",")
                    db.delete(
                        XposedConstants.RUNTIME_MICRO_HOOKS_TABLE,
                        "$MICRO_HOOK_COLUMN_PACKAGE = ? AND $MICRO_HOOK_COLUMN_RECIPE_ID IN ($placeholders)",
                        arrayOf(androidPackageName, *ids.map { it.toString() }.toTypedArray()),
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            true
        }
    }

    override fun deleteAll(file: File): Boolean {
        if (!file.isFile) return false
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            val hasOverrides = db.hasOverrideTable()
            val hasMicroHooks = db.hasMicroHooksTable()
            if (!hasOverrides && !hasMicroHooks) return@use false

            db.beginTransaction()
            try {
                if (hasOverrides) {
                    db.delete(XposedConstants.RUNTIME_OVERRIDES_TABLE, null, null)
                }
                if (hasMicroHooks) {
                    db.delete(XposedConstants.RUNTIME_MICRO_HOOKS_TABLE, null, null)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            true
        }
    }

    private fun editExisting(file: File, operation: (SQLiteDatabase) -> Unit): Boolean {
        if (!file.isFile) return false
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            if (!db.hasOverrideTable()) return@use false
            operation(db)
            true
        }
    }

    private fun SQLiteDatabase.hasOverrideTable(): Boolean = rawQuery(
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
        arrayOf(XposedConstants.RUNTIME_OVERRIDES_TABLE),
    ).use { it.moveToFirst() }

    private fun SQLiteDatabase.hasMicroHooksTable(): Boolean = rawQuery(
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
        arrayOf(XposedConstants.RUNTIME_MICRO_HOOKS_TABLE),
    ).use { it.moveToFirst() }

    private companion object {
        const val MAX_DELETE_BATCH_SIZE = 500
        const val COLUMN_PACKAGE = "packageName"
        const val COLUMN_NAME = "name"
        const val COLUMN_TYPE = "flagType"
        const val COLUMN_VALUE = "value"
        const val READ_SQL = """
            SELECT packageName, name, flagType, value
            FROM ${XposedConstants.RUNTIME_OVERRIDES_TABLE}
            WHERE packageName = ?
            ORDER BY name ASC
        """
        const val CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS ${XposedConstants.RUNTIME_OVERRIDES_TABLE} (
                packageName TEXT NOT NULL,
                name TEXT NOT NULL,
                flagType INTEGER NOT NULL,
                value TEXT NOT NULL,
                PRIMARY KEY(packageName, name)
            )
        """
        const val MICRO_HOOK_COLUMN_PACKAGE = "packageName"
        const val MICRO_HOOK_COLUMN_RECIPE_ID = "recipeId"
        const val MICRO_HOOK_COLUMN_PAYLOAD_BASE64 = "payloadBase64"
        const val MICRO_HOOK_COLUMN_PAYLOAD_SHA256 = "payloadSha256"
        const val MICRO_HOOK_COLUMN_SIGNATURE_BASE64 = "signatureBase64"
        const val MICRO_HOOK_COLUMN_REQUIRED = "required"
        const val CREATE_MICRO_HOOKS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS ${XposedConstants.RUNTIME_MICRO_HOOKS_TABLE} (
                packageName TEXT NOT NULL,
                recipeId INTEGER NOT NULL,
                payloadBase64 TEXT NOT NULL,
                payloadSha256 TEXT NOT NULL,
                signatureBase64 TEXT NOT NULL,
                required INTEGER NOT NULL,
                PRIMARY KEY(packageName, recipeId)
            )
        """
    }
}
