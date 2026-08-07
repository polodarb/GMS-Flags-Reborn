package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecommendationStatusRow(
    applicationStatus: RecommendationApplicationUiStatus,
    supportStatus: RecommendationSupportUiModel,
    onApplicationClick: () -> Unit,
    onSupportClick: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
    ) {
        RecommendationApplicationStatusBadge(
            status = applicationStatus,
            onClick = onApplicationClick,
        )
        RecommendationSupportBadge(
            status = supportStatus,
            onClick = onSupportClick,
        )
    }
}
