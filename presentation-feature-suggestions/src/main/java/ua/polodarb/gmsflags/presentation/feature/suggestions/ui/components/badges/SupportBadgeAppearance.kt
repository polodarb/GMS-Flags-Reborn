package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel

internal data class SupportBadgeAppearance(
    val labelRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
internal fun RecommendationSupportUiModel.appearance(): SupportBadgeAppearance = when (this) {
    RecommendationSupportUiModel.Verified -> SupportBadgeAppearance(
        labelRes = R.string.suggestions_support_verified,
        descriptionRes = R.string.suggestions_support_verified_description,
        icon = Icons.Outlined.Verified,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    RecommendationSupportUiModel.Partial -> SupportBadgeAppearance(
        labelRes = R.string.suggestions_support_partial,
        descriptionRes = R.string.suggestions_support_partial_description,
        icon = Icons.AutoMirrored.Outlined.Rule,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    RecommendationSupportUiModel.Experimental -> SupportBadgeAppearance(
        labelRes = R.string.suggestions_support_experimental,
        descriptionRes = R.string.suggestions_support_experimental_description,
        icon = Icons.Outlined.Science,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
    RecommendationSupportUiModel.Unknown -> SupportBadgeAppearance(
        labelRes = R.string.suggestions_support_unknown,
        descriptionRes = R.string.suggestions_support_unknown_description,
        icon = Icons.AutoMirrored.Outlined.HelpOutline,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
