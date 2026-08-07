package ua.polodarb.gmsflags.data.repository.impl.server.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import ua.polodarb.gmsflags.domain.server.content.LocallyDemotedRecommendationsStore

internal class DataStoreLocallyDemotedRecommendationsStore(
    private val dataStore: DataStore<Preferences>,
) : LocallyDemotedRecommendationsStore {
    override suspend fun read(): Result<Set<Long>> = runCatching {
        dataStore.data.first()[KEY].orEmpty().mapNotNull(String::toLongOrNull).toSet()
    }

    override suspend fun demote(recommendationId: Long): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[KEY] = preferences[KEY].orEmpty() + recommendationId.toString()
        }
    }

    override suspend fun restore(recommendationId: Long): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[KEY] = preferences[KEY].orEmpty() - recommendationId.toString()
        }
    }

    private companion object {
        val KEY = stringSetPreferencesKey("demoted_recommendation_ids")
    }
}
