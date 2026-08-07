package ua.polodarb.gmsflags.data.repository.impl.onboarding

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreOnboardingRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `completion is persisted in DataStore`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(temporaryFolder.root, "onboarding.preferences_pb") },
        )
        val repository = DataStoreOnboardingRepository(dataStore)

        assertFalse(repository.observeCompleted().first())
        assertTrue(repository.markCompleted().isSuccess)
        assertTrue(repository.observeCompleted().first())
    }
}
