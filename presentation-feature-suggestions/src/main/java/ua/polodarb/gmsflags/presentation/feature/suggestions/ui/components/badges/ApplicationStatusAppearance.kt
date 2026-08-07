package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus

internal data class ApplicationStatusAppearance(
    val labelRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
internal fun RecommendationApplicationUiStatus.appearance(): ApplicationStatusAppearance =
    when (this) {
        RecommendationApplicationUiStatus.Applied -> ApplicationStatusAppearance(
            labelRes = R.string.suggestions_details_status_applied,
            descriptionRes = R.string.suggestions_application_status_applied_description,
            icon = Icons.Rounded.CheckCircle,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        RecommendationApplicationUiStatus.PartiallyApplied -> ApplicationStatusAppearance(
            labelRes = R.string.suggestions_details_status_partial,
            descriptionRes = R.string.suggestions_application_status_partial_description,
            icon = Icons.Rounded.Error,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        RecommendationApplicationUiStatus.NotApplied -> ApplicationStatusAppearance(
            labelRes = R.string.suggestions_details_status_not_applied,
            descriptionRes = R.string.suggestions_application_status_not_applied_description,
            icon = Icons.Rounded.RadioButtonUnchecked,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        RecommendationApplicationUiStatus.Unavailable -> ApplicationStatusAppearance(
            labelRes = R.string.suggestions_details_status_unavailable,
            descriptionRes = R.string.suggestions_application_status_unavailable_description,
            icon = Icons.AutoMirrored.Rounded.Help,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RecommendationApplicationUiStatus.Checking -> error("Checking has no badge")
    }
