package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import ua.polodarb.gmsflags.domain.server.content.InfoDisplayMode
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.theme.GmsNestedCardShape
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationInfoUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.ExpandableIconSlot
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.RecommendationDimensions
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.expandCollapseSizeSpec

@Composable
internal fun RecommendationInfoCard(
    infoBlock: RecommendationInfoUiModel,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = GmsNestedCardShape,
) {
    val expandable = infoBlock.displayMode == InfoDisplayMode.Expandable
    var collapsedHeightPx by remember(infoBlock) { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val collapsedHeight = with(density) { collapsedHeightPx.toDp() }
        .coerceAtLeast(RecommendationDimensions.WarningIconSize)

    Surface(
        onClick = onClick,
        enabled = expandable,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(
            modifier = Modifier
                .padding(GmsSpacing.Large)
                .animateContentSize(animationSpec = expandCollapseSizeSpec),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.Top,
            ) {
                ExpandableIconSlot(collapsedHeight) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(RecommendationDimensions.WarningIconSize),
                    )
                }
                if (!expandable || expanded) {
                    Text(
                        infoBlock.message,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        infoBlock.message.lineSequence().first(),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        onTextLayout = { result -> collapsedHeightPx = result.size.height },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (expandable) {
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "infoCardChevronRotation")
                    ExpandableIconSlot(collapsedHeight) {
                        Icon(
                            Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation),
                        )
                    }
                }
            }
        }
    }
}
