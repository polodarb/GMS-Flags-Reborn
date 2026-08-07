package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.theme.GmsNestedCardShape
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.ExpandableIconSlot
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.RecommendationDimensions
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.expandCollapseSizeSpec

private const val WarningCollapsedMaxLines = 2

@Composable
internal fun RecommendationDetailsWarning(
    warning: String,
    modifier: Modifier = Modifier,
    shape: Shape = GmsNestedCardShape,
) {
    var expanded by remember(warning) { mutableStateOf(false) }
    var overflowing by remember(warning) { mutableStateOf(false) }
    var collapsedHeightPx by remember(warning) { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val collapsedHeight = with(density) { collapsedHeightPx.toDp() }
        .coerceAtLeast(RecommendationDimensions.WarningIconSize)

    Surface(
        onClick = { expanded = !expanded },
        enabled = overflowing,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier
                .padding(GmsSpacing.Large)
                .animateContentSize(animationSpec = expandCollapseSizeSpec),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.Top,
        ) {
            ExpandableIconSlot(collapsedHeight) {
                Icon(
                    Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    modifier = Modifier.size(RecommendationDimensions.WarningIconSize),
                )
            }
            Text(
                warning,
                modifier = Modifier.weight(1f),
                maxLines = if (expanded) Int.MAX_VALUE else WarningCollapsedMaxLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!expanded) {
                        overflowing = result.hasVisualOverflow
                        collapsedHeightPx = result.size.height
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (overflowing) {
                val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "detailsWarningChevronRotation")
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
