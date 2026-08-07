package ua.polodarb.gmsflags.data.repository.impl.server.store

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ua.polodarb.gmsflags.domain.server.content.AppliedHookRef
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetup

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreAppliedRecommendationSetupStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `setup is persisted and cleared in DataStore`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = {
                File(temporaryFolder.root, "applied_recommendation_setups.preferences_pb")
            },
        )
        val store = DataStoreAppliedRecommendationSetupStore(dataStore)
        val setup = AppliedRecommendationSetup(
            recommendationId = 8,
            androidPackageName = "com.google.android.dialer",
            flagNamesByPackage = mapOf(
                "com.google.android.dialer" to setOf("primary_flag"),
                "com.google.android.dialer.directboot" to setOf("direct_boot_flag"),
            ),
            hooks = setOf(AppliedHookRef(recipeId = 42, payloadSha256 = "abc123", required = true)),
        )

        assertTrue(store.write(setup).isSuccess)
        assertEquals(setup, store.read(8).getOrThrow())
        assertTrue(store.clear(8).isSuccess)
        assertNull(store.read(8).getOrThrow())
    }

    @Test
    fun `reads a setup stored before the hooks field existed as having no hooks`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = {
                File(temporaryFolder.root, "pre_hooks_field_setup.preferences_pb")
            },
        )
        val store = DataStoreAppliedRecommendationSetupStore(dataStore)
        val legacyJson = """
            {"recommendation_id":8,"android_package":"com.google.android.dialer",
             "flags":[{"package":"com.google.android.dialer","name":"primary_flag"}]}
        """.trimIndent()
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("recommendation_8")] = legacyJson
        }

        val setup = store.read(8).getOrThrow()

        assertEquals(8L, setup?.recommendationId)
        assertEquals(emptySet<AppliedHookRef>(), setup?.hooks)
        assertEquals(
            mapOf("com.google.android.dialer" to setOf("primary_flag")),
            setup?.flagNamesByPackage,
        )
    }

    @Test
    fun `clear all removes every applied recommendation setup`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = {
                File(temporaryFolder.root, "all_setups.preferences_pb")
            },
        )
        val store = DataStoreAppliedRecommendationSetupStore(dataStore)
        val first = setup(recommendationId = 8)
        val second = setup(recommendationId = 9)

        store.write(first).getOrThrow()
        store.write(second).getOrThrow()
        store.clearAll().getOrThrow()

        assertNull(store.read(8).getOrThrow())
        assertNull(store.read(9).getOrThrow())
    }

    private fun setup(recommendationId: Long) = AppliedRecommendationSetup(
        recommendationId = recommendationId,
        androidPackageName = "com.google.android.test",
        flagNamesByPackage = mapOf(
            "test.package" to setOf("flag_a", "flag_b"),
        ),
        hooks = setOf(AppliedHookRef(recipeId = 41L, payloadSha256 = "def456", required = false)),
    )
}
