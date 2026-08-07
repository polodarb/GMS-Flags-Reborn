package ua.polodarb.gmsflags.domain.server.content

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository
import ua.polodarb.gmsflags.data.repository.server.PublicContentRepository
import ua.polodarb.gmsflags.domain.apps.FlagPackage
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

class GetRecommendationFeedUseCaseTest {
    @Test
    fun `status reads each flag from its declared phenotype package`() = runBlocking {
        val queriedPackages = mutableListOf<String>()
        val useCase = GetRecommendationFeedUseCase(
            repository = FakePublicContentRepository(),
            supportedApplicationsRepository = object : SupportedApplicationsRepository {
                override suspend fun getApplications() = Result.success(
                    listOf(
                        SupportedApplication(
                            androidPackageName = ANDROID_PACKAGE,
                            flagPackages = listOf(
                                FlagPackage(PRIMARY_FLAG_PACKAGE, FlagPackageCategory.Primary)
                            ),
                            name = "Phone",
                            versionName = "1.0",
                            versionCode = 100,
                            lastUpdateTime = 0,
                        )
                    )
                )
            },
            flagDetailsRepository = object : FlagDetailsRepository {
                override suspend fun getFlags(
                    androidPackageName: String,
                    phenotypePackageName: String,
                ): Result<List<PhenotypeFlag>> {
                    queriedPackages += phenotypePackageName
                    val flagName = when (phenotypePackageName) {
                        PRIMARY_FLAG_PACKAGE -> "primary_flag"
                        DIRECT_BOOT_FLAG_PACKAGE -> "direct_boot_flag"
                        else -> return Result.failure(IllegalArgumentException(phenotypePackageName))
                    }
                    return Result.success(
                        listOf(
                            PhenotypeFlag(
                                name = flagName,
                                type = FlagType.Boolean,
                                originalValue = "0",
                                value = "1",
                                overridden = true,
                            )
                        )
                    )
                }

                override suspend fun applyOverrides(
                    androidPackageName: String,
                    phenotypePackageName: String,
                    overrides: List<FlagOverride>,
                ) = Result.success(Unit)

                override suspend fun deleteOverride(
                    androidPackageName: String,
                    phenotypePackageName: String,
                    flagName: String,
                ) = Result.success(Unit)

                override suspend fun deleteOverrides(
                    androidPackageName: String,
                    phenotypePackageName: String,
                    flagNames: List<String>,
                ) = Result.success(Unit)

                override suspend fun deletePackageOverrides(
                    androidPackageName: String,
                    phenotypePackageName: String,
                ) = Result.success(Unit)
            },
        )

        val item = useCase().getOrThrow().single()

        assertEquals(
            listOf(PRIMARY_FLAG_PACKAGE, DIRECT_BOOT_FLAG_PACKAGE),
            queriedPackages.sorted(),
        )
        assertEquals(RecommendationApplicationStatus.Applied, item.applicationStatus)
    }

    private class FakePublicContentRepository : PublicContentRepository {
        private val summary = ServerRecommendationSummary(
            id = 8,
            status = RecommendationStatus.Published,
            supportStatus = RecommendationSupportStatus.Verified,
            logoUrl = null,
            title = "Call recording",
            description = null,
            warning = null,
        )

        override suspend fun getRecommendations() = Result.success(listOf(summary))

        override suspend fun getApplications() = Result.success(
            listOf(ServerApplication(4, ANDROID_PACKAGE, "Phone", null))
        )

        override suspend fun getRecommendation(id: Long) = Result.success(
            ServerRecommendationDetails(
                summary = summary,
                applicationId = 4,
                infoBlock = null,
                screenshots = emptyList(),
                externalLink = null,
                source = null,
                variants = listOf(
                    RecommendationFlagVariant(
                        id = 1,
                        label = null,
                        versionConstraint = VersionConstraint(
                            type = VersionConstraintType.Unbounded,
                            minimumVersionCode = null,
                            maximumVersionCode = null,
                        ),
                        flags = listOf(
                            recommendedFlag("primary_flag", packageName = null),
                            recommendedFlag(
                                "direct_boot_flag",
                                packageName = DIRECT_BOOT_FLAG_PACKAGE,
                            ),
                        ),
                    )
                ),
            )
        )

        override suspend fun getHome() = Result.success(emptyList<ServerInfoBlock>())

        override suspend fun getFaq() = Result.success(emptyList<FaqEntry>())

        override suspend fun getApplication(
            packageName: String,
        ): Result<ServerApplicationDetails> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getApplicationRecommendations(
            packageName: String,
        ) = Result.success(emptyList<ServerRecommendationSummary>())
    }

    private companion object {
        const val ANDROID_PACKAGE = "com.google.android.dialer"
        const val PRIMARY_FLAG_PACKAGE = "com.google.android.dialer"
        const val DIRECT_BOOT_FLAG_PACKAGE = "com.google.android.dialer.directboot"

        fun recommendedFlag(name: String, packageName: String?) = RecommendedFlag(
            name = name,
            type = RemoteFlagValueType.Boolean,
            value = "true",
            title = null,
            description = null,
            dangerLevel = DangerLevel.None,
            badges = emptyList(),
            packageName = packageName,
        )
    }
}
