package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.IsOfficialAppBuild
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.ApplyMicroHooks
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverrides
import ua.polodarb.gmsflags.domain.flags.DeleteMicroHooks
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.GetPhenotypeFlags
import ua.polodarb.gmsflags.domain.report.CollectReportDiagnostics
import ua.polodarb.gmsflags.domain.report.ProblemReport
import ua.polodarb.gmsflags.domain.report.ProblemReportContext
import ua.polodarb.gmsflags.domain.report.ReportCategory
import ua.polodarb.gmsflags.domain.report.SubmitProblemReport
import ua.polodarb.gmsflags.domain.server.content.AppliedHookRef
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetup
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetupStore
import ua.polodarb.gmsflags.domain.server.content.DecodeHookRecipe
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationExperience
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationApplicationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.gmsflags.domain.server.content.HookEngineSupport
import ua.polodarb.gmsflags.domain.server.content.VerifyHookTrust
import ua.polodarb.gmsflags.domain.server.content.combineRecommendationApplicationStatuses
import ua.polodarb.gmsflags.domain.server.content.resolveHookApplicationStatus
import ua.polodarb.gmsflags.domain.server.content.resolveRecommendationApplicationStatus
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsContent
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsEffect
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsState
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationApplyTargetUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationApplyAvailability
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationDetailsUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationHookUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationReportPhase
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationReportUiState
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.applyAvailability
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.canSend
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.initialVariantIndex
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.toUiModel

internal class RecommendationDetailsViewModel(
    private val recommendationId: Long,
    private val getRecommendation: GetRecommendationExperience,
    private val getPhenotypeFlags: GetPhenotypeFlags,
    private val applyFlagOverrides: ApplyFlagOverrides,
    private val applyMicroHooks: ApplyMicroHooks,
    private val deleteFlagOverrides: DeleteFlagOverrides,
    private val deleteMicroHooks: DeleteMicroHooks,
    private val appliedSetupStore: AppliedRecommendationSetupStore,
    private val getApplicationScopeStatus: GetApplicationXposedScopeStatus,
    private val verifyHookTrust: VerifyHookTrust,
    private val decodeHookRecipe: DecodeHookRecipe,
    private val hookEngineSupport: HookEngineSupport,
    private val isOfficialAppBuild: IsOfficialAppBuild,
    private val collectReportDiagnostics: CollectReportDiagnostics,
    private val submitProblemReport: SubmitProblemReport,
    private val errorResolver: ErrorResolver,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<RecommendationDetailsEvent, RecommendationDetailsState, RecommendationDetailsEffect>() {
    private var loadJob: Job? = null
    private var applyJob: Job? = null
    private var statusJob: Job? = null
    private var scopeJob: Job? = null
    private var reportJob: Job? = null

    override fun initialState() = RecommendationDetailsState(appIsOfficial = isOfficialAppBuild())

    init {
        load()
    }

    override fun handleEvent(event: RecommendationDetailsEvent) {
        when (event) {
            RecommendationDetailsEvent.BackClicked -> setEffect {
                RecommendationDetailsEffect.NavigateBack
            }
            RecommendationDetailsEvent.Retry -> load()
            RecommendationDetailsEvent.ApplyClicked -> applySelectedVariant()
            RecommendationDetailsEvent.DisableClicked -> disableSelectedVariant()
            RecommendationDetailsEvent.LaunchApplicationClicked -> {
                val recommendation = readyContent()?.recommendation ?: return
                val packageName = recommendation.target?.androidPackageName ?: return
                analytics.track(
                    AnalyticsEvent.recommendationLaunchApp(recommendation.id, packageName)
                )
                setEffect { RecommendationDetailsEffect.LaunchApplication(packageName) }
            }
            RecommendationDetailsEvent.ExternalLinkClicked -> {
                val recommendation = readyContent()?.recommendation ?: return
                recommendation.externalLink?.let { link ->
                    setEffect { RecommendationDetailsEffect.OpenExternalLink(link) }
                }
            }
            RecommendationDetailsEvent.InfoBlockClicked -> setState {
                copy(infoExpanded = !infoExpanded)
            }
            is RecommendationDetailsEvent.VariantSelected -> {
                val recommendation = readyContent()?.recommendation ?: return
                if (recommendation.variants.getOrNull(event.index) == null) return
                setState {
                    copy(
                        selectedVariantIndex = event.index,
                        applicationStatus = RecommendationApplicationUiStatus.Checking,
                        applyAnywayConfirmVisible = false,
                    )
                }
                refreshApplicationStatus(recommendation, event.index)
            }
            RecommendationDetailsEvent.ScopeHelpClicked -> setState { copy(scopeHelpVisible = true) }
            RecommendationDetailsEvent.ScopeHelpDismissed ->
                setState { copy(scopeHelpVisible = false) }
            RecommendationDetailsEvent.ScopeRefresh -> refreshScopeStatus()
            is RecommendationDetailsEvent.FlagNameLongClicked -> setEffect {
                RecommendationDetailsEffect.CopyFlagName(event.name)
            }
            RecommendationDetailsEvent.ApplicationStatusRefresh -> {
                val recommendation = readyContent()?.recommendation ?: return
                refreshApplicationStatus(recommendation, viewState.value.selectedVariantIndex)
            }
            RecommendationDetailsEvent.ApplyAnywayClicked ->
                setState { copy(applyAnywayConfirmVisible = true) }
            RecommendationDetailsEvent.ApplyAnywayDismissed ->
                setState { copy(applyAnywayConfirmVisible = false) }
            RecommendationDetailsEvent.ApplyAnywayConfirmed -> {
                setState { copy(applyAnywayConfirmVisible = false) }
                applySelectedVariant(bypassHookTrustBlock = true)
            }
            RecommendationDetailsEvent.ReportProblemClicked -> openReport(withBlockingHook = true)
            RecommendationDetailsEvent.HeaderReportClicked -> openReport(withBlockingHook = false)
            is RecommendationDetailsEvent.ReportMessageChanged -> setState {
                val report = report ?: return@setState this
                copy(report = report.copy(message = event.message))
            }
            is RecommendationDetailsEvent.ReportContactChanged -> setState {
                val report = report ?: return@setState this
                copy(report = report.copy(contact = event.contact))
            }
            RecommendationDetailsEvent.ReportDetailsToggled -> setState {
                val report = report ?: return@setState this
                copy(report = report.copy(detailsExpanded = !report.detailsExpanded))
            }
            RecommendationDetailsEvent.ReportSendClicked -> sendReport()
            RecommendationDetailsEvent.ReportDismissed -> {
                reportJob?.cancel()
                setState { copy(report = null) }
            }
        }
    }

    private fun openReport(withBlockingHook: Boolean) {
        val recommendation = readyContent()?.recommendation ?: return
        val variantIndex = viewState.value.selectedVariantIndex
        val hook = if (withBlockingHook) blockingHook(recommendation, variantIndex) else null
        val context = buildReportContext(recommendation, variantIndex, hook)
        setState { copy(report = RecommendationReportUiState(context = context)) }
        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            val diagnostics = collectReportDiagnostics().getOrNull()
            setState {
                val report = report ?: return@setState this
                copy(report = report.copy(diagnostics = diagnostics, diagnosticsReady = true))
            }
        }
    }

    private fun sendReport() {
        val state = viewState.value
        val report = state.report ?: return
        if (!report.canSend()) return
        val diagnostics = report.diagnostics ?: return
        val problemReport = ProblemReport(
            message = report.message.trim(),
            contact = report.contact.trim().ifBlank { null },
            diagnostics = diagnostics,
            context = report.context,
            category = ReportCategory.Bug,
        )
        setState {
            copy(report = report.copy(phase = RecommendationReportPhase.Sending))
        }
        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            val phase = submitProblemReport(problemReport).fold(
                onSuccess = { RecommendationReportPhase.Sent },
                onFailure = { RecommendationReportPhase.Error },
            )
            setState {
                val current = this.report ?: return@setState this
                copy(report = current.copy(phase = phase))
            }
        }
    }

    private fun blockingHook(
        recommendation: RecommendationDetailsUiModel,
        variantIndex: Int,
    ): RecommendationHookUiModel? = recommendation.variants
        .getOrNull(variantIndex)
        ?.hooks
        ?.firstOrNull { it.hook.required && it.trustStatus != HookTrustStatus.VERIFIED }

    private fun buildReportContext(
        recommendation: RecommendationDetailsUiModel,
        variantIndex: Int,
        hook: RecommendationHookUiModel?,
    ): ProblemReportContext = ProblemReportContext(
        recommendationId = recommendation.id,
        variantLabel = recommendation.variants.getOrNull(variantIndex)?.label,
        hookRecipeId = hook?.hook?.recipeId,
        hookTrustStatus = hook?.trustStatus?.name,
        targetPackage = recommendation.target?.androidPackageName ?: recommendation.packageName,
        targetVersionName = recommendation.installedVersionName,
    )

    private fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            setState { copy(content = RecommendationDetailsContent.Loading) }
            getRecommendation(recommendationId).fold(
                onSuccess = { experience ->
                    val recommendation = experience.toUiModel(verifyHookTrust, decodeHookRecipe)
                    val initialVariantIndex = recommendation.initialVariantIndex()
                    setState {
                        copy(
                            content = RecommendationDetailsContent.Ready(recommendation),
                            selectedVariantIndex = initialVariantIndex,
                            infoExpanded = false,
                            applying = false,
                            applicationStatus = RecommendationApplicationUiStatus.Checking,
                            applyAnywayConfirmVisible = false,
                        )
                    }
                    analytics.track(
                        AnalyticsEvent.recommendationViewed(recommendation.id, recommendation.title)
                    )
                    refreshApplicationStatus(recommendation, initialVariantIndex)
                    refreshScopeStatus()
                },
                onFailure = { error ->
                    setState {
                        copy(
                            content = RecommendationDetailsContent.Error(
                                errorResolver.resolve(error)
                            ),
                            applying = false,
                        )
                    }
                },
            )
        }
    }

    private fun applySelectedVariant(bypassHookTrustBlock: Boolean = false) {
        if (applyJob?.isActive == true) return
        val state = viewState.value
        val recommendation = (state.content as? RecommendationDetailsContent.Ready)
            ?.recommendation ?: return
        if (recommendation.applyAvailability(state.selectedVariantIndex) !=
            RecommendationApplyAvailability.Available
        ) return
        val target = recommendation.target ?: return
        val selectedVariant = recommendation.variants.getOrNull(state.selectedVariantIndex)
        val groups = selectedVariant?.overridesByPackage.orEmpty()
        val allHooks = selectedVariant?.hooks.orEmpty()

        val hooks: List<RecommendationVariantHook> = when {
            !state.appIsOfficial -> emptyList()
            bypassHookTrustBlock -> allHooks.map { it.hook }
            else -> allHooks.filter { it.trustStatus == HookTrustStatus.VERIFIED }.map { it.hook }
        }
        val totalCount = groups.values.sumOf { it.size } + hooks.size
        if (totalCount == 0) return
        val currentSetup = buildAppliedSetup(
            recommendationId = recommendation.id,
            target = target,
            groups = groups,
            hooks = hooks,
        )

        setState { copy(applying = true, applyAnywayConfirmVisible = false) }
        applyJob = viewModelScope.launch {
            var currentApplyStarted = false
            runCatching {
                val previousSetup = appliedSetupStore.read(recommendation.id).getOrThrow()
                if (previousSetup != null) {
                    deleteAppliedSetup(previousSetup).getOrThrow()
                    appliedSetupStore.clear(recommendation.id).getOrThrow()
                }

                appliedSetupStore.write(currentSetup).getOrThrow()
                currentApplyStarted = true
                groups.entries.fold<Map.Entry<String?, List<FlagOverride>>, Result<Unit>>(
                    Result.success(Unit)
                ) { acc, (packageName, overrides) ->
                    acc.mapCatching {
                        applyFlagOverrides(
                            target.androidPackageName,
                            packageName ?: target.phenotypePackageName,
                            overrides,
                        ).getOrThrow()
                    }
                }.getOrThrow()
                if (hooks.isNotEmpty()) {
                    applyMicroHooks(target.androidPackageName, hooks).getOrThrow()
                }
            }.onFailure {
                if (currentApplyStarted) {
                    val cleanupSucceeded = deleteAppliedSetup(currentSetup).isSuccess
                    if (cleanupSucceeded) {
                        appliedSetupStore.clear(recommendation.id)
                    }
                }
            }.fold(
                onSuccess = {
                    setState {
                        copy(
                            applying = false,
                            applicationStatus = RecommendationApplicationUiStatus.Applied,
                        )
                    }
                    analytics.track(
                        AnalyticsEvent.recommendationVariantApplied(
                            recommendation.id,
                            selectedVariant?.label,
                            totalCount,
                        )
                    )
                    setEffect { RecommendationDetailsEffect.Applied }
                },
                onFailure = { error ->
                    setState { copy(applying = false) }
                    setEffect {
                        RecommendationDetailsEffect.ShowError(errorResolver.resolve(error))
                    }
                },
            )
        }
    }

    private fun disableSelectedVariant() {
        if (applyJob?.isActive == true) return
        val state = viewState.value
        if (state.applicationStatus != RecommendationApplicationUiStatus.Applied) return
        val recommendation = (state.content as? RecommendationDetailsContent.Ready)
            ?.recommendation ?: return
        val target = recommendation.target ?: return
        val selectedVariant = recommendation.variants.getOrNull(state.selectedVariantIndex)
        val groups = selectedVariant?.overridesByPackage.orEmpty()
        val fallbackSetup = buildAppliedSetup(
            recommendationId = recommendation.id,
            target = target,
            groups = groups,
            hooks = selectedVariant?.hooks.orEmpty().map { it.hook },
        )
        if (fallbackSetup.entryCount == 0) return

        setState { copy(applying = true) }
        applyJob = viewModelScope.launch {
            var usedFallback = false
            runCatching {
                val storedSetup = appliedSetupStore.read(recommendation.id).getOrThrow()
                val appliedSetup = storedSetup ?: fallbackSetup.also { usedFallback = true }
                deleteAppliedSetup(appliedSetup).getOrThrow()
                appliedSetupStore.clear(recommendation.id).getOrThrow()
                appliedSetup.entryCount
            }.fold(
                onSuccess = { deletedCount ->
                    setState {
                        copy(
                            applying = false,
                            applicationStatus = RecommendationApplicationUiStatus.NotApplied,
                        )
                    }
                    analytics.track(
                        AnalyticsEvent.recommendationDisabled(recommendation.id, deletedCount)
                    )
                    setEffect {
                        if (usedFallback) {
                            RecommendationDetailsEffect.DisabledUncertain
                        } else {
                            RecommendationDetailsEffect.Disabled
                        }
                    }
                },
                onFailure = { error ->
                    setState { copy(applying = false) }
                    setEffect {
                        RecommendationDetailsEffect.ShowError(errorResolver.resolve(error))
                    }
                },
            )
        }
    }

    private fun buildAppliedSetup(
        recommendationId: Long,
        target: RecommendationApplyTargetUiModel,
        groups: Map<String?, List<FlagOverride>>,
        hooks: List<RecommendationVariantHook>,
    ): AppliedRecommendationSetup {
        val flagNamesByPackage = groups.entries
            .groupBy { (packageName, _) -> packageName ?: target.phenotypePackageName }
            .mapValues { (_, packageEntries) ->
                packageEntries
                    .flatMap { it.value }
                    .mapTo(linkedSetOf()) { it.name }
            }
        return AppliedRecommendationSetup(
            recommendationId = recommendationId,
            androidPackageName = target.androidPackageName,
            flagNamesByPackage = flagNamesByPackage,
            hooks = hooks.mapTo(linkedSetOf()) {
                AppliedHookRef(
                    recipeId = it.recipeId,
                    payloadSha256 = it.envelope?.payloadSha256.orEmpty(),
                    required = it.required,
                )
            },
        )
    }

    private suspend fun deleteAppliedSetup(
        setup: AppliedRecommendationSetup,
    ): Result<Unit> = setup.flagNamesByPackage.entries
        .fold<Map.Entry<String, Set<String>>, Result<Unit>>(Result.success(Unit)) { result, entry ->
            result.mapCatching {
                val packageName = entry.key
                val flagNames = entry.value
                if (flagNames.isNotEmpty()) {
                    deleteFlagOverrides(
                        setup.androidPackageName,
                        packageName,
                        flagNames.toList(),
                    ).getOrThrow()
                }
            }
        }
        .mapCatching {
            if (setup.hooks.isNotEmpty()) {
                deleteMicroHooks(
                    setup.androidPackageName,
                    setup.hooks.map { it.recipeId },
                ).getOrThrow()
            }
        }

    private fun readyContent(): RecommendationDetailsContent.Ready? =
        viewState.value.content as? RecommendationDetailsContent.Ready

    private fun refreshScopeStatus() {
        val packageName = readyContent()?.recommendation?.target?.androidPackageName
        if (packageName == null) {
            setState { copy(xposedScopeStatus = XposedScopeStatus.Included, scopeStatusChecking = false) }
            return
        }
        scopeJob?.cancel()
        scopeJob = viewModelScope.launch {
            val status = getApplicationScopeStatus(packageName)
                .getOrDefault(XposedScopeStatus.Unknown)
            setState { copy(xposedScopeStatus = status, scopeStatusChecking = false) }
        }
    }

    private fun refreshApplicationStatus(
        recommendation: RecommendationDetailsUiModel,
        variantIndex: Int,
    ) {
        statusJob?.cancel()
        val target = recommendation.target
        val variant = recommendation.variants.getOrNull(variantIndex)
        val groups = variant?.overridesByPackage.orEmpty()
        val expectedHooks = variant?.hooks.orEmpty()
            .filter { it.trustStatus == HookTrustStatus.VERIFIED }
            .map { it.hook }
        if (target == null || (groups.values.sumOf { it.size } == 0 && expectedHooks.isEmpty())) {
            setState { copy(applicationStatus = RecommendationApplicationUiStatus.Unavailable) }
            return
        }

        statusJob = viewModelScope.launch {
            val statuses = mutableListOf<RecommendationApplicationStatus>()
            var unavailable = false
            for ((packageName, overrides) in groups) {
                val groupStatus = getPhenotypeFlags(
                    target.androidPackageName,
                    packageName ?: target.phenotypePackageName,
                ).fold(
                    onSuccess = { flags ->
                        resolveRecommendationApplicationStatus(overrides, flags)
                    },
                    onFailure = { null },
                )
                if (groupStatus == null) {
                    unavailable = true
                    break
                }
                statuses += groupStatus
            }
            if (!unavailable && expectedHooks.isNotEmpty()) {
                val appliedHooks = appliedSetupStore.read(recommendation.id)
                    .getOrNull()
                    ?.hooks
                    .orEmpty()
                val unsupportedRequiredRecipeIds = expectedHooks
                    .filter { it.required && !hookEngineSupport(it) }
                    .map { it.recipeId }
                    .toSet()
                statuses += resolveHookApplicationStatus(expectedHooks, appliedHooks, unsupportedRequiredRecipeIds)
            }
            val status = if (unavailable) {
                RecommendationApplicationUiStatus.Unavailable
            } else {
                combineRecommendationApplicationStatuses(statuses).toUiStatus()
            }
            if (viewState.value.selectedVariantIndex == variantIndex) {
                setState { copy(applicationStatus = status) }
            }
        }
    }
}

private fun RecommendationApplicationStatus.toUiStatus() = when (this) {
    RecommendationApplicationStatus.Applied -> RecommendationApplicationUiStatus.Applied
    RecommendationApplicationStatus.PartiallyApplied ->
        RecommendationApplicationUiStatus.PartiallyApplied
    RecommendationApplicationStatus.NotApplied -> RecommendationApplicationUiStatus.NotApplied
    RecommendationApplicationStatus.Unavailable -> RecommendationApplicationUiStatus.Unavailable
    RecommendationApplicationStatus.ClientUpdateRequired -> RecommendationApplicationUiStatus.Unavailable
}

private val AppliedRecommendationSetup.entryCount: Int
    get() = flagNamesByPackage.values.sumOf { it.size } + hooks.size
