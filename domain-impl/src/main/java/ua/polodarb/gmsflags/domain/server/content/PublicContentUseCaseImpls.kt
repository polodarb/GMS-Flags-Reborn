package ua.polodarb.gmsflags.domain.server.content

import ua.polodarb.gmsflags.data.repository.server.PublicContentRepository
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class GetHomeContentUseCase(
    private val repository: PublicContentRepository,
) : GetHomeContent {
    override suspend fun invoke() = repository.getHome()
}

class GetFaqUseCase(
    private val repository: PublicContentRepository,
) : GetFaq {
    override suspend fun invoke() = repository.getFaq()
}

class GetServerApplicationsUseCase(
    private val repository: PublicContentRepository,
) : GetServerApplications {
    override suspend fun invoke() = repository.getApplications()
}

class GetServerApplicationUseCase(
    private val repository: PublicContentRepository,
) : GetServerApplication {
    override suspend fun invoke(packageName: String) =
        repository.getApplication(packageName.requirePackageName())
}

class GetApplicationRecommendationsUseCase(
    private val repository: PublicContentRepository,
) : GetApplicationRecommendations {
    override suspend fun invoke(packageName: String) =
        repository.getApplicationRecommendations(packageName.requirePackageName())
}

class GetRecommendationsUseCase(
    private val repository: PublicContentRepository,
) : GetRecommendations {
    override suspend fun invoke() = repository.getRecommendations()
}

class GetRecommendationFeedUseCase(
    private val repository: PublicContentRepository,
    private val supportedApplicationsRepository: SupportedApplicationsRepository,
    private val flagDetailsRepository: FlagDetailsRepository,
    private val verifyHookTrust: VerifyHookTrust,
    private val hookEngineSupport: HookEngineSupport,
    private val appliedSetupStore: AppliedRecommendationSetupStore,
) : GetRecommendationFeed {
    override suspend fun invoke(): Result<List<ServerRecommendationFeedItem>> = runCatching {
        val summaries = repository.getRecommendations().getOrThrow()
        val serverApplications = repository.getApplications().getOrDefault(emptyList())
            .associateBy { it.id }
        val installedApplications = supportedApplicationsRepository.getApplications()
            .getOrDefault(emptyList())
            .associateBy { it.androidPackageName }
        val semaphore = Semaphore(MAX_CONCURRENT_DETAILS_REQUESTS)
        withContext(Dispatchers.Default) {
            summaries.map { summary ->
                async {
                    val details = semaphore.withPermit {
                        repository.getRecommendation(summary.id).getOrNull()
                    }
                    val recommendedApp = details?.let { serverApplications[it.applicationId] }
                    val recommendedInstalled = recommendedApp?.packageName
                        ?.let(installedApplications::get)
                    val unsupported = recommendedApp?.disabledFromVersion?.let { disabledFrom ->
                        recommendedInstalled != null &&
                            recommendedInstalled.versionCode >= disabledFrom
                    } == true
                    if (unsupported) return@async null
                    ServerRecommendationFeedItem(
                        summary = summary,
                        application = recommendedApp,
                        previewScreenshotUrl = details?.screenshots?.firstOrNull(),
                        flagNames = details?.variants
                            ?.flatMap { it.flags }
                            ?.map { it.name }
                            ?.distinct()
                            .orEmpty(),
                        applicationStatus = details?.let { recommendation ->
                            val installed = recommendedInstalled
                            val target = installed?.mainFlagPackage
                            val variant = installed?.versionCode?.let { versionCode ->
                                recommendation.variants.firstOrNull {
                                    it.versionConstraint.matches(versionCode)
                                }
                            }
                            val expectedByPackage = variant?.flags
                                ?.mapNotNull { flag ->
                                    flag.toFlagOverrideOrNull()?.let { override ->
                                        (flag.packageName?.trim()?.takeIf(String::isNotEmpty)
                                            ?: target?.packageName) to override
                                    }
                                }
                                ?.filter { it.first != null }
                                ?.groupBy(
                                    keySelector = { it.first!! },
                                    valueTransform = { it.second },
                                )
                                .orEmpty()
                            val expectedHooks = variant?.hooks.orEmpty()
                                .filter { verifyHookTrust(it) == HookTrustStatus.VERIFIED }
                            if (installed == null || target == null ||
                                (expectedByPackage.isEmpty() && expectedHooks.isEmpty())
                            ) {
                                RecommendationApplicationStatus.Unavailable
                            } else {
                                val flagStatuses = expectedByPackage.map { (packageName, expected) ->
                                    flagDetailsRepository.getFlags(
                                        installed.androidPackageName,
                                        packageName,
                                    ).fold(
                                        onSuccess = { flags ->
                                            resolveRecommendationApplicationStatus(expected, flags)
                                        },
                                        onFailure = { RecommendationApplicationStatus.Unavailable },
                                    )
                                }
                                val hookStatuses = if (expectedHooks.isEmpty()) {
                                    emptyList()
                                } else {
                                    val appliedHooks = appliedSetupStore.read(summary.id)
                                        .getOrNull()
                                        ?.hooks
                                        .orEmpty()
                                    val unsupportedRequiredRecipeIds = expectedHooks
                                        .filter { it.required && !hookEngineSupport(it) }
                                        .map { it.recipeId }
                                        .toSet()
                                    listOf(
                                        resolveHookApplicationStatus(
                                            expectedHooks,
                                            appliedHooks,
                                            unsupportedRequiredRecipeIds,
                                        )
                                    )
                                }
                                combineRecommendationApplicationStatuses(flagStatuses + hookStatuses)
                            }
                        } ?: RecommendationApplicationStatus.Unavailable,
                    )
                }
            }.awaitAll().filterNotNull()
        }
    }

    private companion object {
        const val MAX_CONCURRENT_DETAILS_REQUESTS = 4
    }
}

class GetRecommendationDetailsUseCase(
    private val repository: PublicContentRepository,
) : GetRecommendationDetails {
    override suspend fun invoke(id: Long) = repository.getRecommendation(id.requireId())
}

class GetRecommendationExperienceUseCase(
    private val publicContentRepository: PublicContentRepository,
    private val supportedApplicationsRepository: SupportedApplicationsRepository,
) : GetRecommendationExperience {
    override suspend fun invoke(id: Long): Result<RecommendationExperience> = runCatching {
        val details = publicContentRepository.getRecommendation(id.requireId()).getOrThrow()
        val application = publicContentRepository.getApplications()
            .getOrDefault(emptyList())
            .firstOrNull { it.id == details.applicationId }
        val installedApplication = application?.packageName?.let { packageName ->
            supportedApplicationsRepository.getApplications()
                .getOrDefault(emptyList())
                .firstOrNull { it.androidPackageName == packageName }
        }
        RecommendationExperience(
            details = details,
            application = application,
            installedApplication = installedApplication,
        )
    }
}

private fun String.requirePackageName(): String = trim().also {
    require(it.isNotEmpty()) { "Package name cannot be blank" }
}

private fun Long.requireId(): Long = also {
    require(it > 0L) { "Recommendation id must be positive" }
}
