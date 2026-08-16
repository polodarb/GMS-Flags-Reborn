package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model

import ua.polodarb.gmsflags.domain.server.content.DecodeHookRecipe
import ua.polodarb.gmsflags.domain.server.content.RecommendationExperience
import ua.polodarb.gmsflags.domain.server.content.VerifyHookTrust
import ua.polodarb.gmsflags.domain.server.content.VersionConstraintType
import ua.polodarb.gmsflags.domain.server.content.matches
import ua.polodarb.gmsflags.domain.server.content.toFlagOverrideOrNull
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.toUiModel

internal fun RecommendationExperience.toUiModel(
    verifyHookTrust: VerifyHookTrust,
    decodeHookRecipe: DecodeHookRecipe,
): RecommendationDetailsUiModel {
    val installed = installedApplication
    val recommendationInfo = details.infoBlock
    val installedVersionCode = installed?.versionCode
    return RecommendationDetailsUiModel(
        id = details.summary.id,
        title = details.summary.title.trim(),
        description = details.summary.description.normalizedText(),
        logoUrl = details.summary.logoUrl.normalizedText(),
        supportStatus = details.summary.supportStatus.toUiModel(),
        warning = details.summary.warning.normalizedText(),
        infoBlock = recommendationInfo?.let { info ->
            info.message.normalizedText()?.let { message ->
                RecommendationInfoUiModel(message, info.displayMode)
            }
        },
        screenshots = details.screenshots.mapNotNull(String::normalizedText).distinct(),
        externalLink = details.externalLink.normalizedText(),
        source = details.source.normalizedText(),
        applicationName = application?.displayName.normalizedText()
            ?: installed?.name.normalizedText(),
        packageName = application?.packageName.normalizedText(),
        applicationInstalled = installed != null,
        installedVersionName = installed?.versionName.normalizedText(),
        target = installed?.mainFlagPackage?.let { flagPackage ->
            RecommendationApplyTargetUiModel(
                androidPackageName = installed.androidPackageName,
                phenotypePackageName = flagPackage.packageName,
            )
        },
        variants = details.variants.map { variant ->
            val mappedFlags = variant.flags.map { flag ->
                val override = flag.toFlagOverrideOrNull()
                RecommendedFlagUiModel(
                    name = flag.name,
                    title = flag.title.normalizedText(),
                    description = flag.description.normalizedText(),
                    type = flag.type,
                    value = flag.value,
                    dangerLevel = flag.dangerLevel,
                    badges = flag.badges.mapNotNull { badge ->
                        badge.label.normalizedText()?.let {
                            RecommendationBadgeUiModel(it)
                        }
                    },
                    supported = override != null,
                    packageName = flag.packageName.normalizedText(),
                )
            }
            val overridesByPackage = variant.flags
                .mapNotNull { flag ->
                    flag.toFlagOverrideOrNull()?.let { flag.packageName.normalizedText() to it }
                }
                .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            val overrides = overridesByPackage.values.flatten()
            RecommendationVariantUiModel(
                label = variant.label.normalizedText(),
                description = variant.description.normalizedText(),
                versionLabel = VersionConstraintUiModel(
                    type = when (variant.versionConstraint.type) {
                        VersionConstraintType.Unbounded -> VersionConstraintTypeUiModel.Any
                        VersionConstraintType.From -> VersionConstraintTypeUiModel.From
                        VersionConstraintType.Until -> VersionConstraintTypeUiModel.Until
                        VersionConstraintType.Range -> VersionConstraintTypeUiModel.Range
                        VersionConstraintType.Unknown -> VersionConstraintTypeUiModel.Unknown
                    },
                    minimum = variant.versionConstraint.minimumVersionCode,
                    maximum = variant.versionConstraint.maximumVersionCode,
                ),
                compatibility = when (installedVersionCode) {
                    null -> VariantCompatibilityUiModel.Unknown
                    else -> if (variant.versionConstraint.matches(installedVersionCode)) {
                        VariantCompatibilityUiModel.Compatible
                    } else {
                        VariantCompatibilityUiModel.Incompatible
                    }
                },
                flags = mappedFlags,
                overrides = overrides,
                overridesByPackage = overridesByPackage,
                unsupportedFlagCount = mappedFlags.count { !it.supported },
                hooks = variant.hooks.map { hook ->
                    RecommendationHookUiModel(
                        hook = hook,
                        trustStatus = verifyHookTrust(hook),
                        recipeDetails = decodeHookRecipe(hook).getOrNull(),
                    )
                },
            )
        },
    )
}

internal fun RecommendationDetailsUiModel.initialVariantIndex(): Int =
    variants.indexOfFirst { it.compatibility == VariantCompatibilityUiModel.Compatible }
        .takeIf { it >= 0 }
        ?: 0

internal fun RecommendationDetailsUiModel.applyAvailability(
    selectedVariantIndex: Int,
): RecommendationApplyAvailability {
    val variant = variants.getOrNull(selectedVariantIndex)
        ?: return RecommendationApplyAvailability.NoFlags
    if (applicationName == null || packageName == null) {
        return RecommendationApplyAvailability.ApplicationUnknown
    }
    if (!applicationInstalled) {
        return RecommendationApplyAvailability.ApplicationNotInstalled
    }
    if (target == null) return RecommendationApplyAvailability.FlagPackageUnavailable
    if (variant.compatibility != VariantCompatibilityUiModel.Compatible) {
        return RecommendationApplyAvailability.VersionUnsupported
    }
    if (variant.unsupportedFlagCount > 0) {
        return RecommendationApplyAvailability.ContainsUnsupportedFlags
    }
    if (variant.overrides.isEmpty() && variant.hooks.isEmpty()) return RecommendationApplyAvailability.NoFlags
    return RecommendationApplyAvailability.Available
}

private fun String?.normalizedText(): String? = this?.trim()?.takeIf(String::isNotEmpty)
