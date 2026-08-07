package ua.polodarb.gmsflags.data.repository.impl.settings.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.impl.flags.TargetProcessRestarter
import ua.polodarb.gmsflags.data.repository.impl.settings.datasource.OverrideControlPreferences
import ua.polodarb.gmsflags.data.repository.settings.datasource.OverrideControlDataSource
import ua.polodarb.gmsflags.domain.apps.FlagPackage
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetup
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetupStore

class OverrideControlRepositoryImplTest {
    private val dataSource = FakeDataSource()
    private val restarter = FakeRestarter()
    private val preferences = FakePreferences()
    private val appliedSetupStore = FakeAppliedSetupStore()
    private val repository = OverrideControlRepositoryImpl(
        applicationsRepository = FakeApplicationsRepository(),
        dataSource = dataSource,
        targetRestarter = restarter,
        preferences = preferences,
        appliedSetupStore = appliedSetupStore,
    )

    @Test
    fun `refresh exposes saved count`() = runBlocking {
        dataSource.count = 7

        repository.refresh().getOrThrow()

        assertEquals(7, repository.state.value.overrideCount)
        assertFalse(repository.state.value.paused)
    }

    @Test
    fun `pause preserves state and restarts each target once`() = runBlocking {
        repository.setPaused(true).getOrThrow()

        assertTrue(preferences.paused)
        assertTrue(repository.state.value.paused)
        assertEquals(listOf(APP_PACKAGE), dataSource.pauseTargets)
        assertEquals(listOf(APP_PACKAGE), restarter.restarted)
    }

    @Test
    fun `delete all clears count and restarts target`() = runBlocking {
        dataSource.count = 3
        repository.refresh().getOrThrow()

        repository.deleteAll().getOrThrow()

        assertEquals(0, repository.state.value.overrideCount)
        assertEquals(listOf(APP_PACKAGE), dataSource.deletedTargets)
        assertEquals(listOf(APP_PACKAGE), restarter.restarted)
        assertEquals(1, appliedSetupStore.clearAllCalls)
    }

    private class FakeApplicationsRepository : SupportedApplicationsRepository {
        override suspend fun getApplications() = Result.success(
            listOf(
                SupportedApplication(
                    androidPackageName = APP_PACKAGE,
                    flagPackages = listOf(
                        FlagPackage("keep", FlagPackageCategory.Primary)
                    ),
                    name = "Keep",
                    versionName = "1",
                    versionCode = 1,
                    lastUpdateTime = 0,
                )
            )
        )
    }

    private class FakeDataSource : OverrideControlDataSource {
        var count = 0
        var pauseTargets = emptyList<String>()
        var deletedTargets = emptyList<String>()

        override suspend fun readOverrideCount(androidPackageNames: List<String>) = Result.success(count)
        override suspend fun readPaused(androidPackageNames: List<String>) = Result.success(false)
        override suspend fun setPaused(androidPackageNames: List<String>, paused: Boolean): Result<Unit> {
            pauseTargets = androidPackageNames
            return Result.success(Unit)
        }
        override suspend fun deleteAll(androidPackageNames: List<String>): Result<Unit> {
            deletedTargets = androidPackageNames
            return Result.success(Unit)
        }
    }

    private class FakeRestarter : TargetProcessRestarter {
        val restarted = mutableListOf<String>()
        override suspend fun restart(androidPackageName: String): Result<Unit> {
            restarted += androidPackageName
            return Result.success(Unit)
        }
    }

    private class FakePreferences : OverrideControlPreferences {
        override var paused: Boolean = false
    }

    private class FakeAppliedSetupStore : AppliedRecommendationSetupStore {
        var clearAllCalls = 0

        override suspend fun read(recommendationId: Long) =
            Result.success<AppliedRecommendationSetup?>(null)

        override suspend fun write(setup: AppliedRecommendationSetup) = Result.success(Unit)

        override suspend fun clear(recommendationId: Long) = Result.success(Unit)

        override suspend fun clearAll(): Result<Unit> {
            clearAllCalls += 1
            return Result.success(Unit)
        }
    }

    private companion object {
        const val APP_PACKAGE = "com.google.android.keep"
    }
}
