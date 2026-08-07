package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.analytics.AnalyticsUserProperty
import ua.polodarb.gmsflags.domain.apps.FlagPackage
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.IsOfficialAppBuild
import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.ApplyMicroHooks
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverrides
import ua.polodarb.gmsflags.domain.flags.DeleteMicroHooks
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.GetPhenotypeFlags
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.domain.report.CollectReportDiagnostics
import ua.polodarb.gmsflags.domain.report.ProblemReport
import ua.polodarb.gmsflags.domain.report.ReportCategory
import ua.polodarb.gmsflags.domain.report.ReportDeviceInfo
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics
import ua.polodarb.gmsflags.domain.report.SubmitProblemReport
import ua.polodarb.gmsflags.domain.server.content.AppliedHookRef
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetup
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetupStore
import ua.polodarb.gmsflags.domain.server.content.DangerLevel
import ua.polodarb.gmsflags.domain.server.content.DecodeHookRecipe
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationExperience
import ua.polodarb.gmsflags.domain.server.content.RecommendationExperience
import ua.polodarb.gmsflags.domain.server.content.RecommendationFlagVariant
import ua.polodarb.gmsflags.domain.server.content.RecommendationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationSupportStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendedFlag
import ua.polodarb.gmsflags.domain.server.content.RemoteFlagValueType
import ua.polodarb.gmsflags.domain.server.content.ServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.domain.server.content.VerifyHookTrust
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.domain.server.content.VersionConstraint
import ua.polodarb.gmsflags.domain.server.content.VersionConstraintType
import ua.polodarb.gmsflags.presentation.core.error.DefaultErrorResolver
import ua.polodarb.gmsflags.presentation.feature.suggestions.MainDispatcherRule
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsContent
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsEvent

private object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) {}
    override fun setUserProperty(property: AnalyticsUserProperty, value: String) {}
    override fun setCollectionEnabled(enabled: Boolean) {}
}

private class InMemoryAppliedRecommendationSetupStore(
    initial: AppliedRecommendationSetup? = null,
) : AppliedRecommendationSetupStore {
    var setup: AppliedRecommendationSetup? = initial

    override suspend fun read(
        recommendationId: Long,
    ): Result<AppliedRecommendationSetup?> = Result.success(setup)

    override suspend fun write(setup: AppliedRecommendationSetup): Result<Unit> {
        this.setup = setup
        return Result.success(Unit)
    }

    override suspend fun clear(recommendationId: Long): Result<Unit> {
        setup = null
        return Result.success(Unit)
    }

    override suspend fun clearAll(): Result<Unit> {
        setup = null
        return Result.success(Unit)
    }
}

private val NoOpReportDiagnostics = CollectReportDiagnostics {
    Result.success(
        ReportDiagnostics(
            device = ReportDeviceInfo(
                manufacturer = "Google",
                model = "Pixel",
                androidRelease = "17",
                sdkInt = 37,
                abi = "arm64-v8a",
            ),
            appVersionName = "1.0",
            appVersionCode = 1,
            appSignatureSha256 = null,
        )
    )
}

private val NoOpSubmitProblemReport = SubmitProblemReport { Result.success(Unit) }

private val NoOpDecodeHookRecipe = DecodeHookRecipe {
    Result.failure(IllegalStateException("no envelope in tests"))
}

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationDetailsViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `applies selected compatible recommendation once`() = runTest(dispatcherRule.dispatcher) {
        var applied: List<FlagOverride> = emptyList()
        val appliedSetupStore = InMemoryAppliedRecommendationSetupStore()
        val viewModel = RecommendationDetailsViewModel(
            recommendationId = 7,
            getRecommendation = GetRecommendationExperience { Result.success(experience) },
            getPhenotypeFlags = GetPhenotypeFlags { _, _ -> Result.success(emptyList()) },
            applyFlagOverrides = ApplyFlagOverrides { _, _, overrides ->
                applied = overrides
                Result.success(Unit)
            },
            deleteFlagOverrides = DeleteFlagOverrides { _, _, _ -> Result.success(Unit) },
            deleteMicroHooks = DeleteMicroHooks { _, _ -> Result.success(Unit) },
            appliedSetupStore = appliedSetupStore,
            getApplicationScopeStatus = GetApplicationXposedScopeStatus {
                Result.success(XposedScopeStatus.Included)
            },
            applyMicroHooks = ApplyMicroHooks { _, _ -> Result.success(Unit) },
            verifyHookTrust = VerifyHookTrust { HookTrustStatus.NOT_SIGNED },
            decodeHookRecipe = NoOpDecodeHookRecipe,
            isOfficialAppBuild = IsOfficialAppBuild { true },
            collectReportDiagnostics = NoOpReportDiagnostics,
            submitProblemReport = NoOpSubmitProblemReport,
            errorResolver = DefaultErrorResolver(),
            analytics = NoOpAnalyticsTracker,
        )
        advanceUntilIdle()

        viewModel.setEvent(RecommendationDetailsEvent.ApplyClicked)
        advanceUntilIdle()

        assertEquals(listOf("feature_enabled"), applied.map { it.name })
        assertEquals(
            mapOf("com.google.flags" to setOf("feature_enabled")),
            appliedSetupStore.setup?.flagNamesByPackage,
        )
        assertFalse(viewModel.viewState.value.applying)
        assertEquals(
            RecommendationApplicationUiStatus.Applied,
            viewModel.viewState.value.applicationStatus,
        )
        assert(viewModel.viewState.value.content is RecommendationDetailsContent.Ready)
    }

    @Test
    fun `ApplicationStatusRefresh re-reads current flags instead of trusting the optimistic Applied status`() =
        runTest(dispatcherRule.dispatcher) {
            val notOverridden = listOf(
                PhenotypeFlag(
                    name = "feature_enabled",
                    type = FlagType.Boolean,
                    originalValue = "0",
                    value = "0",
                    overridden = false,
                )
            )
            val viewModel = RecommendationDetailsViewModel(
                recommendationId = 7,
                getRecommendation = GetRecommendationExperience { Result.success(experience) },
                getPhenotypeFlags = GetPhenotypeFlags { _, _ -> Result.success(notOverridden) },
                applyFlagOverrides = ApplyFlagOverrides { _, _, _ -> Result.success(Unit) },
                deleteFlagOverrides = DeleteFlagOverrides { _, _, _ -> Result.success(Unit) },
                deleteMicroHooks = DeleteMicroHooks { _, _ -> Result.success(Unit) },
                appliedSetupStore = InMemoryAppliedRecommendationSetupStore(),
                getApplicationScopeStatus = GetApplicationXposedScopeStatus {
                    Result.success(XposedScopeStatus.Included)
                },
                applyMicroHooks = ApplyMicroHooks { _, _ -> Result.success(Unit) },
                verifyHookTrust = VerifyHookTrust { HookTrustStatus.NOT_SIGNED },
            decodeHookRecipe = NoOpDecodeHookRecipe,
                isOfficialAppBuild = IsOfficialAppBuild { true },
                collectReportDiagnostics = NoOpReportDiagnostics,
                submitProblemReport = NoOpSubmitProblemReport,
                errorResolver = DefaultErrorResolver(),
                analytics = NoOpAnalyticsTracker,
            )
            advanceUntilIdle()

            viewModel.setEvent(RecommendationDetailsEvent.ApplyClicked)
            advanceUntilIdle()
            assertEquals(
                RecommendationApplicationUiStatus.Applied,
                viewModel.viewState.value.applicationStatus,
            )

            viewModel.setEvent(RecommendationDetailsEvent.ApplicationStatusRefresh)
            advanceUntilIdle()

            assertEquals(
                RecommendationApplicationUiStatus.NotApplied,
                viewModel.viewState.value.applicationStatus,
            )
        }

    @Test
    fun `disables applied recommendation by deleting only its flags`() =
        runTest(dispatcherRule.dispatcher) {
            var deleted = emptyList<String>()
            val viewModel = RecommendationDetailsViewModel(
                recommendationId = 7,
                getRecommendation = GetRecommendationExperience { Result.success(experience) },
                getPhenotypeFlags = GetPhenotypeFlags { _, _ ->
                    Result.success(
                        listOf(
                            PhenotypeFlag(
                                name = "feature_enabled",
                                type = FlagType.Boolean,
                                originalValue = "0",
                                value = "1",
                                overridden = true,
                            )
                        )
                    )
                },
                applyFlagOverrides = ApplyFlagOverrides { _, _, _ -> Result.success(Unit) },
                deleteFlagOverrides = DeleteFlagOverrides { _, _, names ->
                    deleted = names
                    Result.success(Unit)
                },
                deleteMicroHooks = DeleteMicroHooks { _, _ -> Result.success(Unit) },
                appliedSetupStore = InMemoryAppliedRecommendationSetupStore(),
                getApplicationScopeStatus = GetApplicationXposedScopeStatus {
                    Result.success(XposedScopeStatus.Included)
                },
                applyMicroHooks = ApplyMicroHooks { _, _ -> Result.success(Unit) },
                verifyHookTrust = VerifyHookTrust { HookTrustStatus.NOT_SIGNED },
            decodeHookRecipe = NoOpDecodeHookRecipe,
                isOfficialAppBuild = IsOfficialAppBuild { true },
                collectReportDiagnostics = NoOpReportDiagnostics,
                submitProblemReport = NoOpSubmitProblemReport,
                errorResolver = DefaultErrorResolver(),
                analytics = NoOpAnalyticsTracker,
            )
            advanceUntilIdle()

            viewModel.setEvent(RecommendationDetailsEvent.DisableClicked)
            advanceUntilIdle()

            assertEquals(listOf("feature_enabled"), deleted)
            assertFalse(viewModel.viewState.value.applying)
            assertEquals(
                RecommendationApplicationUiStatus.NotApplied,
                viewModel.viewState.value.applicationStatus,
            )
        }

    @Test
    fun `reapply deletes the exact previous setup before writing the current setup`() =
        runTest(dispatcherRule.dispatcher) {
            val operations = mutableListOf<String>()
            val store = InMemoryAppliedRecommendationSetupStore(
                AppliedRecommendationSetup(
                    recommendationId = 7,
                    androidPackageName = "com.google.app",
                    flagNamesByPackage = mapOf(
                        "com.google.old.flags" to setOf("old_one", "old_two")
                    ),
                    hooks = setOf(AppliedHookRef(recipeId = 42, payloadSha256 = "old-hash", required = true)),
                )
            )
            val viewModel = RecommendationDetailsViewModel(
                recommendationId = 7,
                getRecommendation = GetRecommendationExperience { Result.success(experience) },
                getPhenotypeFlags = GetPhenotypeFlags { _, _ -> Result.success(emptyList()) },
                applyFlagOverrides = ApplyFlagOverrides { _, packageName, overrides ->
                    operations += "apply:$packageName:${overrides.joinToString { it.name }}"
                    Result.success(Unit)
                },
                applyMicroHooks = ApplyMicroHooks { _, _ -> Result.success(Unit) },
                deleteFlagOverrides = DeleteFlagOverrides { _, packageName, names ->
                    operations += "delete:$packageName:${names.sorted().joinToString()}"
                    Result.success(Unit)
                },
                deleteMicroHooks = DeleteMicroHooks { _, recipeIds ->
                    operations += "delete-hooks:${recipeIds.sorted().joinToString()}"
                    Result.success(Unit)
                },
                appliedSetupStore = store,
                getApplicationScopeStatus = GetApplicationXposedScopeStatus {
                    Result.success(XposedScopeStatus.Included)
                },
                verifyHookTrust = VerifyHookTrust { HookTrustStatus.NOT_SIGNED },
            decodeHookRecipe = NoOpDecodeHookRecipe,
                isOfficialAppBuild = IsOfficialAppBuild { true },
                collectReportDiagnostics = NoOpReportDiagnostics,
                submitProblemReport = NoOpSubmitProblemReport,
                errorResolver = DefaultErrorResolver(),
                analytics = NoOpAnalyticsTracker,
            )
            advanceUntilIdle()

            viewModel.setEvent(RecommendationDetailsEvent.ApplyClicked)
            advanceUntilIdle()

            assertEquals(
                listOf(
                    "delete:com.google.old.flags:old_one, old_two",
                    "delete-hooks:42",
                    "apply:com.google.flags:feature_enabled",
                ),
                operations,
            )
            assertEquals(
                mapOf("com.google.flags" to setOf("feature_enabled")),
                store.setup?.flagNamesByPackage,
            )
            assertTrue(store.setup?.hooks.orEmpty().isEmpty())
        }

    @Test
    fun `disable removes the stored setup after the server flag set changed`() =
        runTest(dispatcherRule.dispatcher) {
            val deletedFlags = mutableListOf<String>()
            val deletedHooks = mutableListOf<Long>()
            val store = InMemoryAppliedRecommendationSetupStore(
                AppliedRecommendationSetup(
                    recommendationId = 7,
                    androidPackageName = "com.google.app",
                    flagNamesByPackage = mapOf(
                        "com.google.previous.flags" to setOf("removed_from_recommendation")
                    ),
                    hooks = setOf(AppliedHookRef(recipeId = 91, payloadSha256 = "prev-hash", required = false)),
                )
            )
            val viewModel = RecommendationDetailsViewModel(
                recommendationId = 7,
                getRecommendation = GetRecommendationExperience { Result.success(experience) },
                getPhenotypeFlags = GetPhenotypeFlags { _, _ ->
                    Result.success(
                        listOf(
                            PhenotypeFlag(
                                name = "feature_enabled",
                                type = FlagType.Boolean,
                                originalValue = "0",
                                value = "1",
                                overridden = true,
                            )
                        )
                    )
                },
                applyFlagOverrides = ApplyFlagOverrides { _, _, _ -> Result.success(Unit) },
                applyMicroHooks = ApplyMicroHooks { _, _ -> Result.success(Unit) },
                deleteFlagOverrides = DeleteFlagOverrides { _, _, names ->
                    deletedFlags += names
                    Result.success(Unit)
                },
                deleteMicroHooks = DeleteMicroHooks { _, recipeIds ->
                    deletedHooks += recipeIds
                    Result.success(Unit)
                },
                appliedSetupStore = store,
                getApplicationScopeStatus = GetApplicationXposedScopeStatus {
                    Result.success(XposedScopeStatus.Included)
                },
                verifyHookTrust = VerifyHookTrust { HookTrustStatus.NOT_SIGNED },
            decodeHookRecipe = NoOpDecodeHookRecipe,
                isOfficialAppBuild = IsOfficialAppBuild { true },
                collectReportDiagnostics = NoOpReportDiagnostics,
                submitProblemReport = NoOpSubmitProblemReport,
                errorResolver = DefaultErrorResolver(),
                analytics = NoOpAnalyticsTracker,
            )
            advanceUntilIdle()

            viewModel.setEvent(RecommendationDetailsEvent.DisableClicked)
            advanceUntilIdle()

            assertEquals(listOf("removed_from_recommendation"), deletedFlags)
            assertEquals(listOf(91L), deletedHooks)
            assertEquals(null, store.setup)
        }

    @Test
    fun `an unofficial app build never writes hooks, even a VERIFIED one`() = runTest(dispatcherRule.dispatcher) {
        var hooksApplied: List<RecommendationVariantHook>? = null
        val experienceWithHook = experience.copy(
            details = experience.details.copy(
                variants = listOf(
                    experience.details.variants.single().copy(
                        hooks = listOf(
                            RecommendationVariantHook(
                                recipeId = 42,
                                required = false,
                                envelope = null,
                            )
                        ),
                    )
                ),
            ),
        )
        val viewModel = RecommendationDetailsViewModel(
            recommendationId = 7,
            getRecommendation = GetRecommendationExperience { Result.success(experienceWithHook) },
            getPhenotypeFlags = GetPhenotypeFlags { _, _ -> Result.success(emptyList()) },
            applyFlagOverrides = ApplyFlagOverrides { _, _, _ -> Result.success(Unit) },
            deleteFlagOverrides = DeleteFlagOverrides { _, _, _ -> Result.success(Unit) },
            deleteMicroHooks = DeleteMicroHooks { _, _ -> Result.success(Unit) },
            appliedSetupStore = InMemoryAppliedRecommendationSetupStore(),
            getApplicationScopeStatus = GetApplicationXposedScopeStatus {
                Result.success(XposedScopeStatus.Included)
            },
            applyMicroHooks = ApplyMicroHooks { _, hooks ->
                hooksApplied = hooks
                Result.success(Unit)
            },
            verifyHookTrust = VerifyHookTrust { HookTrustStatus.VERIFIED },
            decodeHookRecipe = NoOpDecodeHookRecipe,
            isOfficialAppBuild = IsOfficialAppBuild { false },
            collectReportDiagnostics = NoOpReportDiagnostics,
            submitProblemReport = NoOpSubmitProblemReport,
            errorResolver = DefaultErrorResolver(),
            analytics = NoOpAnalyticsTracker,
        )
        advanceUntilIdle()

        assertFalse(viewModel.viewState.value.appIsOfficial)
        viewModel.setEvent(RecommendationDetailsEvent.ApplyClicked)
        advanceUntilIdle()

        assertEquals(null, hooksApplied)
        assertEquals(
            RecommendationApplicationUiStatus.Applied,
            viewModel.viewState.value.applicationStatus,
        )
    }

    @Test
    fun `HeaderReportClicked then ReportSendClicked submits category Bug`() = runTest(dispatcherRule.dispatcher) {
        var submitted: ProblemReport? = null
        val viewModel = RecommendationDetailsViewModel(
            recommendationId = 7,
            getRecommendation = GetRecommendationExperience { Result.success(experience) },
            getPhenotypeFlags = GetPhenotypeFlags { _, _ -> Result.success(emptyList()) },
            applyFlagOverrides = ApplyFlagOverrides { _, _, _ -> Result.success(Unit) },
            deleteFlagOverrides = DeleteFlagOverrides { _, _, _ -> Result.success(Unit) },
            deleteMicroHooks = DeleteMicroHooks { _, _ -> Result.success(Unit) },
            appliedSetupStore = InMemoryAppliedRecommendationSetupStore(),
            getApplicationScopeStatus = GetApplicationXposedScopeStatus {
                Result.success(XposedScopeStatus.Included)
            },
            applyMicroHooks = ApplyMicroHooks { _, _ -> Result.success(Unit) },
            verifyHookTrust = VerifyHookTrust { HookTrustStatus.NOT_SIGNED },
            decodeHookRecipe = NoOpDecodeHookRecipe,
            isOfficialAppBuild = IsOfficialAppBuild { true },
            collectReportDiagnostics = NoOpReportDiagnostics,
            submitProblemReport = SubmitProblemReport { report ->
                submitted = report
                Result.success(Unit)
            },
            errorResolver = DefaultErrorResolver(),
            analytics = NoOpAnalyticsTracker,
        )
        advanceUntilIdle()

        viewModel.setEvent(RecommendationDetailsEvent.HeaderReportClicked)
        advanceUntilIdle()
        viewModel.setEvent(RecommendationDetailsEvent.ReportMessageChanged("it's broken"))
        viewModel.setEvent(RecommendationDetailsEvent.ReportSendClicked)
        advanceUntilIdle()

        assertEquals(ReportCategory.Bug, submitted?.category)
    }

    private companion object {
        val experience = RecommendationExperience(
            details = ServerRecommendationDetails(
                summary = ServerRecommendationSummary(
                    id = 7,
                    status = RecommendationStatus.Published,
                    supportStatus = RecommendationSupportStatus.Verified,
                    logoUrl = null,
                    title = "Feature",
                    description = null,
                    warning = null,
                ),
                applicationId = 4,
                infoBlock = null,
                screenshots = emptyList(),
                externalLink = null,
                source = null,
                variants = listOf(
                    RecommendationFlagVariant(
                        id = 1,
                        label = "Default",
                        versionConstraint = VersionConstraint(
                            VersionConstraintType.Unbounded,
                            null,
                            null,
                        ),
                        flags = listOf(
                            RecommendedFlag(
                                name = "feature_enabled",
                                type = RemoteFlagValueType.Boolean,
                                value = "true",
                                title = null,
                                description = null,
                                dangerLevel = DangerLevel.None,
                                badges = emptyList(),
                            )
                        ),
                    )
                ),
            ),
            application = ServerApplication(4, "com.google.app", "Google App", null),
            installedApplication = SupportedApplication(
                androidPackageName = "com.google.app",
                flagPackages = listOf(
                    FlagPackage("com.google.flags", FlagPackageCategory.Primary)
                ),
                name = "Google App",
                versionName = "1.0",
                versionCode = 100,
                lastUpdateTime = 0,
            ),
        )
    }
}
