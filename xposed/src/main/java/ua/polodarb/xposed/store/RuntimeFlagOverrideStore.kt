package ua.polodarb.xposed.store

import android.database.sqlite.SQLiteDatabase
import ua.polodarb.xposed.info.XposedConstants
import ua.polodarb.xposed.info.phenotypePackageCandidates
import java.io.File
import ua.polodarb.xposed.logging.XposedLogger

internal class RuntimeFlagOverrideStore(
    private val dbFile: File
) {

    private var cachedModifiedAt = Long.MIN_VALUE
    private var cachedOverrides = emptyMap<Key, Override>()

    fun find(packageName: String, flagName: String): Override? {
        refreshIfNeeded()
        return cachedOverrides[Key(packageName, flagName)]
    }

    fun hasOverrides(): Boolean {
        refreshIfNeeded()
        return cachedOverrides.isNotEmpty()
    }

    fun overrideCount(): Int {
        refreshIfNeeded()
        return cachedOverrides.size
    }

    fun findBestMatch(
        identityPackageName: String,
        contextPackageName: String,
        flagName: String,
    ): Match {
        refreshIfNeeded()

        phenotypePackageCandidates(identityPackageName, contextPackageName).forEach { candidate ->
            cachedOverrides[Key(candidate.packageName, flagName)]?.let {
                return Match(it, candidate.source)
            }
        }

        return Match(null, "miss")
    }

    fun findBestMatches(
        identityPackageName: String,
        contextPackageName: String,
    ): Map<String, Match> {
        refreshIfNeeded()
        val matches = linkedMapOf<String, Match>()

        phenotypePackageCandidates(identityPackageName, contextPackageName).forEach { candidate ->
            cachedOverrides.forEach { (key, override) ->
                if (key.packageName == candidate.packageName) {
                    matches.putIfAbsent(
                        key.flagName,
                        Match(override, candidate.source),
                    )
                }
            }
        }

        return matches
    }

    fun describeForLog(): String {
        refreshIfNeeded()
        val packages = cachedOverrides.keys
            .groupingBy(Key::packageName)
            .eachCount()
            .entries
            .sortedByDescending(Map.Entry<String, Int>::value)
            .take(MAX_LOG_PACKAGES)
            .joinToString(prefix = "[", postfix = "]") { (packageName, count) ->
                "$packageName($count)"
            }
        return "path=${dbFile.path}, exists=${dbFile.isFile}, count=${cachedOverrides.size}, " +
            "packages=$packages"
    }

    private fun refreshIfNeeded() {
        val modifiedAt = dbFile.takeIf { it.isFile }?.lastModified() ?: Long.MIN_VALUE
        if (modifiedAt == cachedModifiedAt) return

        cachedModifiedAt = modifiedAt
        cachedOverrides = if (modifiedAt == Long.MIN_VALUE) {
            emptyMap()
        } else {
            readOverrides()
        }
    }

    private fun readOverrides(): Map<Key, Override> {
        val result = linkedMapOf<Key, Override>()
        val db = runCatching {
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        }.getOrElse {
            XposedLogger.logW("Runtime overrides DB is not readable: ${it.message}")
            return emptyMap()
        }

        db.use {
            runCatching {
                it.rawQuery(
                    """
                    SELECT packageName, name, flagType, value
                    FROM ${XposedConstants.RUNTIME_OVERRIDES_TABLE};
                    """.trimIndent(),
                    null
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val packageName = cursor.getString(0) ?: continue
                        val flagName = cursor.getString(1) ?: continue
                        result[Key(packageName, flagName)] = Override(
                            flagType = cursor.getInt(2),
                            value = cursor.getString(3) ?: ""
                        )
                    }
                }
            }.onFailure { error ->
                XposedLogger.logW("Failed to read runtime overrides: ${error.message}")
            }
        }

        return result
    }

    data class Override(
        val flagType: Int,
        val value: String,
    )

    data class Match(
        val override: Override?,
        val source: String,
    )

    private data class Key(
        val packageName: String,
        val flagName: String,
    )

    private companion object {
        const val MAX_LOG_PACKAGES = 8
    }
}
