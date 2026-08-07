package ua.polodarb.gmsflags.data.repository.impl.server.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import ua.polodarb.gmsflags.domain.server.content.AppliedHookRef
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetup
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetupStore

internal class DataStoreAppliedRecommendationSetupStore(
    private val dataStore: DataStore<Preferences>,
) : AppliedRecommendationSetupStore {
    override suspend fun read(recommendationId: Long): Result<AppliedRecommendationSetup?> =
        runCatching {
            val encoded = dataStore.data.first()[key(recommendationId)]
                ?: return@runCatching null
            decode(encoded, recommendationId)
        }

    override suspend fun write(setup: AppliedRecommendationSetup): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[key(setup.recommendationId)] = encode(setup)
        }
    }

    override suspend fun clear(recommendationId: Long): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences.remove(key(recommendationId))
        }
    }

    override suspend fun clearAll(): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun encode(setup: AppliedRecommendationSetup): String = buildJsonObject {
        put(FIELD_RECOMMENDATION_ID, setup.recommendationId)
        put(FIELD_ANDROID_PACKAGE, setup.androidPackageName)
        put(
            FIELD_FLAGS,
            buildJsonArray {
                setup.flagNamesByPackage
                    .toSortedMap()
                    .forEach { (packageName, names) ->
                        names.sorted().forEach { name ->
                            add(
                                buildJsonObject {
                                    put(FIELD_FLAG_PACKAGE, packageName)
                                    put(FIELD_FLAG_NAME, name)
                                }
                            )
                        }
                    }
            },
        )
        put(
            FIELD_HOOKS,
            buildJsonArray {
                setup.hooks.sortedBy { it.recipeId }.forEach { hook ->
                    add(
                        buildJsonObject {
                            put(FIELD_HOOK_RECIPE_ID, hook.recipeId)
                            put(FIELD_HOOK_PAYLOAD_SHA256, hook.payloadSha256)
                            put(FIELD_HOOK_REQUIRED, hook.required)
                        }
                    )
                }
            },
        )
    }.toString()

    private fun decode(
        encoded: String,
        recommendationId: Long,
    ): AppliedRecommendationSetup {
        val root = json.parseToJsonElement(encoded).jsonObject
        val storedRecommendationId = root.getValue(FIELD_RECOMMENDATION_ID)
            .jsonPrimitive.longOrNull
            ?: error("Invalid recommendation id")
        require(storedRecommendationId == recommendationId) {
            "Stored recommendation id does not match key"
        }
        val flags = root.getValue(FIELD_FLAGS).jsonArray
            .map { it.jsonObject }
            .groupBy(
                keySelector = {
                    it.getValue(FIELD_FLAG_PACKAGE).jsonPrimitive.content
                },
                valueTransform = {
                    it.getValue(FIELD_FLAG_NAME).jsonPrimitive.content
                },
            )
            .mapValues { (_, names) -> names.toSet() }
        val hooks = (root[FIELD_HOOKS] as? JsonArray).orEmpty()
            .map { it.jsonObject }
            .map { hook ->
                AppliedHookRef(
                    recipeId = hook.getValue(FIELD_HOOK_RECIPE_ID).jsonPrimitive.longOrNull
                        ?: error("Invalid hook recipe id"),
                    payloadSha256 = hook.getValue(FIELD_HOOK_PAYLOAD_SHA256).jsonPrimitive.content,
                    required = hook.getValue(FIELD_HOOK_REQUIRED).jsonPrimitive.boolean,
                )
            }
            .toSet()
        return AppliedRecommendationSetup(
            recommendationId = storedRecommendationId,
            androidPackageName = root.getValue(FIELD_ANDROID_PACKAGE).jsonPrimitive.content,
            flagNamesByPackage = flags,
            hooks = hooks,
        )
    }

    private fun key(recommendationId: Long) =
        stringPreferencesKey("$KEY_PREFIX$recommendationId")

    private companion object {
        val json = Json { ignoreUnknownKeys = true }

        const val KEY_PREFIX = "recommendation_"
        const val FIELD_RECOMMENDATION_ID = "recommendation_id"
        const val FIELD_ANDROID_PACKAGE = "android_package"
        const val FIELD_FLAGS = "flags"
        const val FIELD_FLAG_PACKAGE = "package"
        const val FIELD_FLAG_NAME = "name"
        const val FIELD_HOOKS = "hooks"
        const val FIELD_HOOK_RECIPE_ID = "recipe_id"
        const val FIELD_HOOK_PAYLOAD_SHA256 = "payload_sha256"
        const val FIELD_HOOK_REQUIRED = "required"
    }
}
