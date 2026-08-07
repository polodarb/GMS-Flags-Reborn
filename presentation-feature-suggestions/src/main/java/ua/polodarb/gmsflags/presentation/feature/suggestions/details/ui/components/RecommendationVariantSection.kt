package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationApplyAvailability
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationDetailsUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationVariantUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.VariantCompatibilityUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.VersionConstraintTypeUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.applyAvailability

@Composable
internal fun RecommendationVariantSection(
    recommendation: RecommendationDetailsUiModel,
    selectedVariantIndex: Int,
    onVariantSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
) {
    val selectedVariant = recommendation.variants.getOrNull(selectedVariantIndex)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Text(
                text = stringResource(R.string.suggestions_details_configuration),
                style = MaterialTheme.typography.titleLarge,
            )
            if (recommendation.variants.size > 1) {
                RecommendationVariantPicker(
                    variants = recommendation.variants,
                    selectedVariantIndex = selectedVariantIndex,
                    onVariantSelected = onVariantSelected,
                )
            }
            selectedVariant?.let { variant ->
                Text(
                    text = variant.versionText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VariantCompatibilityRow(variant.compatibility)
                val availability = recommendation.applyAvailability(selectedVariantIndex)
                if (availability != RecommendationApplyAvailability.Available) {
                    Text(
                        text = stringResource(availability.explanationRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row {
                    Text(
                        text = pluralStringResource(
                            R.plurals.suggestions_details_flags_count,
                            variant.flags.size,
                            variant.flags.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (variant.hooks.isNotEmpty()) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.suggestions_details_hooks_count,
                                variant.hooks.size,
                                variant.hooks.size,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } ?: Text(
                text = stringResource(R.string.suggestions_details_no_variants),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VariantCompatibilityRow(compatibility: VariantCompatibilityUiModel) {
    val icon = when (compatibility) {
        VariantCompatibilityUiModel.Compatible -> Icons.Outlined.CheckCircle
        VariantCompatibilityUiModel.Incompatible -> Icons.Outlined.ErrorOutline
        VariantCompatibilityUiModel.Unknown -> Icons.AutoMirrored.Outlined.HelpOutline
    }
    val text = when (compatibility) {
        VariantCompatibilityUiModel.Compatible -> R.string.suggestions_details_compatible
        VariantCompatibilityUiModel.Incompatible -> R.string.suggestions_details_incompatible
        VariantCompatibilityUiModel.Unknown -> R.string.suggestions_details_compatibility_unknown
    }
    val color = when (compatibility) {
        VariantCompatibilityUiModel.Compatible -> MaterialTheme.colorScheme.primary
        VariantCompatibilityUiModel.Incompatible -> MaterialTheme.colorScheme.error
        VariantCompatibilityUiModel.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
        Icon(icon, contentDescription = null, tint = color)
        Text(stringResource(text), color = color, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun RecommendationVariantUiModel.versionText(): String = when (versionLabel.type) {
    VersionConstraintTypeUiModel.Any -> stringResource(R.string.suggestions_details_any_version)
    VersionConstraintTypeUiModel.From -> stringResource(
        R.string.suggestions_details_version_from,
        versionLabel.minimum ?: 0,
    )
    VersionConstraintTypeUiModel.Until -> stringResource(
        R.string.suggestions_details_version_until,
        versionLabel.maximum ?: 0,
    )
    VersionConstraintTypeUiModel.Range -> stringResource(
        R.string.suggestions_details_version_range,
        versionLabel.minimum ?: 0,
        versionLabel.maximum ?: 0,
    )
    VersionConstraintTypeUiModel.Unknown -> stringResource(
        R.string.suggestions_details_version_unknown
    )
}

private fun RecommendationApplyAvailability.explanationRes(): Int = when (this) {
    RecommendationApplyAvailability.ApplicationUnknown ->
        R.string.suggestions_details_reason_app_unknown
    RecommendationApplyAvailability.ApplicationNotInstalled ->
        R.string.suggestions_details_reason_not_installed
    RecommendationApplyAvailability.FlagPackageUnavailable ->
        R.string.suggestions_details_reason_flag_package
    RecommendationApplyAvailability.VersionUnsupported ->
        R.string.suggestions_details_reason_version
    RecommendationApplyAvailability.ContainsUnsupportedFlags ->
        R.string.suggestions_details_reason_types
    RecommendationApplyAvailability.NoFlags -> R.string.suggestions_details_reason_empty
    RecommendationApplyAvailability.Available -> R.string.suggestions_details_compatible
}
