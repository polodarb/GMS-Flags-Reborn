package ua.polodarb.gmsflags.domain.hookstatus

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.hookstatus.repository.HookStatusRepository
import ua.polodarb.gmsflags.domain.apps.SupportedApplication

class GetHookStatusUseCaseTest {
    @Test
    fun `working applications are sorted before the rest`() = runBlocking {
        val idle = application(name = "Alpha", packageName = "app.alpha")
        val working = application(name = "Zulu", packageName = "app.zulu")
        val useCase = GetHookStatusUseCase(
            applicationsRepository = FakeApplicationsRepository(listOf(idle, working)),
            hookStatusRepository = FakeHookStatusRepository(
                listOf(workingRecord(working.androidPackageName))
            ),
        )

        val result = useCase().getOrThrow()

        assertEquals(listOf("Zulu", "Alpha"), result.applications.map { it.applicationName })
    }

    @Test
    fun `propagates compatibility warnings and counts them as attention`() = runBlocking {
        val application = application(name = "Recorder", packageName = "app.recorder")
        val useCase = GetHookStatusUseCase(
            applicationsRepository = FakeApplicationsRepository(listOf(application)),
            hookStatusRepository = FakeHookStatusRepository(
                listOf(
                    HookDiagnosticRecord(
                        androidPackageName = application.androidPackageName,
                        currentOverrideCount = 0,
                        compatibilityWarnings = setOf(HookCompatibilityWarning.PairipCore),
                    )
                )
            ),
        )

        val result = useCase().getOrThrow()

        assertEquals(
            setOf(HookCompatibilityWarning.PairipCore),
            result.applications.single().compatibilityWarnings,
        )
        assertEquals(1, result.attentionApplicationCount)
        assertEquals(0, result.workingApplicationCount)
    }

    private fun application(name: String, packageName: String) = SupportedApplication(
        androidPackageName = packageName,
        flagPackages = emptyList(),
        name = name,
        versionName = null,
        versionCode = 1,
        lastUpdateTime = 0,
    )

    private fun workingRecord(packageName: String) = HookDiagnosticRecord(
        androidPackageName = packageName,
        currentOverrideCount = 1,
        session = HookSession(
            processName = packageName,
            versionCode = 1,
            startedAt = 1,
            updatedAt = 1,
            loadedOverrideCount = 1,
            state = HookSessionState.Installed,
            strategies = listOf(
                HookStrategyDiagnostic(
                    name = "test",
                    state = HookStrategyState.Installed,
                    appliedCount = 1,
                    consumedCount = 1,
                )
            ),
        ),
    )

    private class FakeApplicationsRepository(
        private val applications: List<SupportedApplication>,
    ) : SupportedApplicationsRepository {
        override suspend fun getApplications() = Result.success(applications)
    }

    private class FakeHookStatusRepository(
        private val records: List<HookDiagnosticRecord>,
    ) : HookStatusRepository {
        override suspend fun readDiagnostics(androidPackageNames: List<String>) =
            Result.success(records)

        override suspend fun restart(androidPackageName: String) = Result.success(Unit)
    }
}
