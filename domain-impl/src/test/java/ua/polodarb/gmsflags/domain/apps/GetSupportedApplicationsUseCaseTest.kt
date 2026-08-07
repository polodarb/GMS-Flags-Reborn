package ua.polodarb.gmsflags.domain.apps

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository

class GetSupportedApplicationsUseCaseTest {
    @Test
    fun `deduplicates and sorts supported applications`() = runBlocking {
        val repository = object : SupportedApplicationsRepository {
            override suspend fun getApplications() = Result.success(
                listOf(app("z.app", "Zulu"), app("a.app", "Alpha"), app("z.app", "Zulu"))
            )
        }

        assertEquals(
            listOf("a.app", "z.app"),
            GetSupportedApplicationsUseCase(repository)().getOrThrow().map { it.androidPackageName },
        )
    }

    @Test
    fun `main flag package prefers exact application package`() {
        val application = app("target.app", "Target").copy(
            flagPackages = listOf(
                FlagPackage("a.primary", FlagPackageCategory.Primary),
                FlagPackage("target.app", FlagPackageCategory.Primary),
                FlagPackage("z.secondary", FlagPackageCategory.Secondary),
            )
        )

        assertEquals("target.app", application.mainFlagPackage?.packageName)
    }

    @Test
    fun `main flag package falls back to categorized primary`() {
        val application = app("target.app", "Target").copy(
            flagPackages = listOf(
                FlagPackage("a.secondary", FlagPackageCategory.Secondary),
                FlagPackage("z.primary", FlagPackageCategory.Primary),
            )
        )

        assertEquals("z.primary", application.mainFlagPackage?.packageName)
    }

    @Test
    fun `main flag package uses explicit preference first`() {
        val application = app("com.android.vending", "Play Store").copy(
            flagPackages = listOf(
                FlagPackage("com.google.android.finsky.instantapps", FlagPackageCategory.Primary),
                FlagPackage("com.google.android.finsky.regular", FlagPackageCategory.Primary),
                FlagPackage("com.google.android.finsky.stable", FlagPackageCategory.Primary),
            ),
            preferredFlagPackageName = "com.google.android.finsky.stable",
        )

        assertEquals("com.google.android.finsky.stable", application.mainFlagPackage?.packageName)
    }

    private fun app(packageName: String, name: String) = SupportedApplication(
        androidPackageName = packageName,
        flagPackages = listOf(
            FlagPackage(packageName = packageName, category = FlagPackageCategory.Primary)
        ),
        name = name,
        versionName = null,
        versionCode = 1,
        lastUpdateTime = 0,
    )
}
