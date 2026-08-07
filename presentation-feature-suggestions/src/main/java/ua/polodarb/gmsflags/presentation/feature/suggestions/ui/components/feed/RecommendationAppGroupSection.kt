package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.feed

import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards.RecommendationCard
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards.RecommendationLogo
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.RecommendationDimensions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.shape.gmsGroupPositionOf
import ua.polodarb.gmsflags.presentation.core.ui.shape.gmsGroupedCardShape
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationAppGroupUiModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RecommendationAppGroupSection(
    group: RecommendationAppGroupUiModel,
    expanded: Boolean,
    onToggle: () -> Unit,
    onRecommendationSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupCount = if (expanded) 1 + group.recommendations.size else 1
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val rotationSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = rememberHapticClick(onClick = onToggle),
            modifier = Modifier.fillMaxWidth(),
            shape = gmsGroupedCardShape(gmsGroupPositionOf(0, groupCount)),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = GmsSpacing.Medium)
                    .heightIn(min = RecommendationDimensions.AppGroupLogoSize),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val iconZoneWidth by animateDpAsState(
                    targetValue = if (expanded) {
                        GmsSpacing.Large
                    } else {
                        GmsSpacing.Large + RecommendationDimensions.AppGroupLogoSize + GmsSpacing.Large
                    },
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>(),
                    label = "appGroupIconZoneWidth",
                )
                val iconAlpha = animateFloatAsState(
                    targetValue = if (expanded) 0f else 1f,
                    animationSpec = effectsSpec,
                    label = "appGroupIconAlpha",
                )
                Box(
                    modifier = Modifier.width(iconZoneWidth),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    RecommendationLogo(
                        logoUrl = group.iconUrl,
                        size = RecommendationDimensions.AppGroupLogoSize,
                        contentPadding = GmsSpacing.Small,
                        modifier = Modifier
                            .padding(start = GmsSpacing.Large)
                            .graphicsLayer { alpha = iconAlpha.value },
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    Text(
                        text = group.displayName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.suggestions_recommendations_count,
                            group.recommendations.size,
                            group.recommendations.size,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                val rotation by animateFloatAsState(
                    if (expanded) 180f else 0f,
                    animationSpec = rotationSpatialSpec,
                    label = "appGroupChevronRotation",
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = GmsSpacing.Large, end = GmsSpacing.Large)
                        .rotate(rotation),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = spatialSpec) +
                fadeIn(animationSpec = effectsSpec),
            exit = shrinkVertically(animationSpec = spatialSpec) +
                fadeOut(animationSpec = effectsSpec),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = GmsSpacing.ExtraSmall),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                group.recommendations.forEachIndexed { index, recommendation ->
                    key(recommendation.id) {
                        RecommendationCard(
                            recommendation = recommendation,
                            onClick = { onRecommendationSelected(recommendation.id) },
                            shape = gmsGroupedCardShape(gmsGroupPositionOf(1 + index, groupCount)),
                            modifier = Modifier.fillMaxWidth(),
                            showFeaturedBadge = false,
                        )
                    }
                }
            }
        }
    }
}
