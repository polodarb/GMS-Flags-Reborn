package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model

import androidx.compose.runtime.Immutable
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.server.content.DangerLevel
import ua.polodarb.gmsflags.domain.server.content.HookRecipeDetails
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.domain.server.content.InfoDisplayMode
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.gmsflags.domain.server.content.RemoteFlagValueType
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel

@Immutable
data class RecommendationDetailsUiModel(
    val id: Long,
    val title: String,
    val description: String?,
    val logoUrl: String?,
    val supportStatus: RecommendationSupportUiModel,
    val warning: String?,
    val infoBlock: RecommendationInfoUiModel?,
    val screenshots: List<String>,
    val externalLink: String?,
    val source: String?,
    val applicationName: String?,
    val packageName: String?,
    val applicationInstalled: Boolean,
    val installedVersionName: String?,
    val target: RecommendationApplyTargetUiModel?,
    val variants: List<RecommendationVariantUiModel>,
)

@Immutable
data class RecommendationInfoUiModel(
    val message: String,
    val displayMode: InfoDisplayMode,
)

@Immutable
data class RecommendationApplyTargetUiModel(
    val androidPackageName: String,
    val phenotypePackageName: String,
)

@Immutable
data class RecommendationVariantUiModel(
    val label: String?,
    val description: String?,
    val versionLabel: VersionConstraintUiModel,
    val compatibility: VariantCompatibilityUiModel,
    val flags: List<RecommendedFlagUiModel>,
    val overrides: List<FlagOverride>,
    val overridesByPackage: Map<String?, List<FlagOverride>>,
    val unsupportedFlagCount: Int,
    val hooks: List<RecommendationHookUiModel>,
)

/**
 * Wraps a [RecommendationVariantHook] with its locally-computed [trustStatus], without adding a
 * verification-specific field onto the pure domain model. [hook] retains the full signed envelope
 * so the apply flow can still write it; [trustStatus] is what the UI renders. [recipeDetails] is
 * the decoded selector+effect for the "view instructions" sheet - null whenever the envelope is
 * missing or fails to decode, in which case that entry point is simply not shown.
 */
@Immutable
data class RecommendationHookUiModel(
    val hook: RecommendationVariantHook,
    val trustStatus: HookTrustStatus,
    val recipeDetails: HookRecipeDetails?,
)

@Immutable
data class VersionConstraintUiModel(
    val type: VersionConstraintTypeUiModel,
    val minimum: Long?,
    val maximum: Long?,
)

enum class VersionConstraintTypeUiModel { Any, From, Until, Range, Unknown }
enum class VariantCompatibilityUiModel { Compatible, Incompatible, Unknown }

@Immutable
data class RecommendedFlagUiModel(
    val name: String,
    val title: String?,
    val description: String?,
    val type: RemoteFlagValueType,
    val value: String,
    val dangerLevel: DangerLevel,
    val badges: List<RecommendationBadgeUiModel>,
    val supported: Boolean,
    val packageName: String?,
)

@Immutable
data class RecommendationBadgeUiModel(
    val label: String,
)

enum class RecommendationApplyAvailability {
    Available,
    ApplicationUnknown,
    ApplicationNotInstalled,
    FlagPackageUnavailable,
    VersionUnsupported,
    ContainsUnsupportedFlags,
    NoFlags,
}
