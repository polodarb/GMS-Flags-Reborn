package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.RecommendationDimensions
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel

@Composable
internal fun RecommendationSupportBadge(
    status: RecommendationSupportUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appearance = status.appearance()
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = appearance.containerColor,
        contentColor = appearance.contentColor,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Medium,
                vertical = GmsSpacing.Small,
            ),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = appearance.icon,
                contentDescription = null,
                modifier = Modifier.size(RecommendationDimensions.SupportIconSize),
            )
            Text(
                text = stringResource(appearance.labelRes),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
