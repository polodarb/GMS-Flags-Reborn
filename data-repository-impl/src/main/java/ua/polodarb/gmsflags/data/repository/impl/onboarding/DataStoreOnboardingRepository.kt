package ua.polodarb.gmsflags.data.repository.impl.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ua.polodarb.gmsflags.data.repository.onboarding.OnboardingRepository

internal class DataStoreOnboardingRepository(
    private val dataStore: DataStore<Preferences>,
) : OnboardingRepository {
    override fun observeCompleted(): Flow<Boolean> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences -> preferences[CompletedKey] ?: false }

    override suspend fun markCompleted(): Result<Unit> = runCatching {
        dataStore.edit { preferences -> preferences[CompletedKey] = true }
    }

    private companion object {
        val CompletedKey = booleanPreferencesKey("onboarding_completed")
    }
}
