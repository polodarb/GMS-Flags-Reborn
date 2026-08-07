package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards

import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges.RecommendationFeaturedBadge
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges.RecommendationStatusRow
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.sheets.RecommendationApplicationInfoSheet
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.sheets.RecommendationSupportInfoSheet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsScopeWarningVisibility
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeActionChip
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components.RecommendationInfoCard
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationUiModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val FeaturedBadgeEnterSpring = MotionScheme.expressive().defaultSpatialSpec<Float>()

@Composable
internal fun RecommendationCard(
    recommendation: RecommendationUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    showFeaturedBadge: Boolean = recommendation.pinned,
) {
    var supportInfoVisible by remember(recommendation.id) { mutableStateOf(false) }
    var applicationInfoVisible by remember(recommendation.id) { mutableStateOf(false) }
    var infoBlockExpanded by remember(recommendation.id) { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            onClick = rememberHapticClick(onClick = onClick),
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GmsSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RecommendationLogo(logoUrl = recommendation.logoUrl)
                    Text(
                        text = recommendation.title,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                RecommendationStatusRow(
                    applicationStatus = recommendation.applicationStatus,
                    supportStatus = recommendation.supportStatus,
                    onApplicationClick = { applicationInfoVisible = true },
                    onSupportClick = { supportInfoVisible = true },
                )

                recommendation.description?.let { description ->
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                recommendation.warning?.let { warning ->
                    RecommendationWarning(warning = warning)
                }

                recommendation.infoBlock?.let { infoBlock ->
                    RecommendationInfoCard(
                        infoBlock = infoBlock,
                        expanded = infoBlockExpanded,
                        onClick = { infoBlockExpanded = !infoBlockExpanded },
                    )
                }

                GmsScopeWarningVisibility(visible = recommendation.scopeExcluded) {
                    GmsXposedScopeActionChip(
                        label = stringResource(R.string.suggestions_scope_missing_badge),
                        onClick = onClick,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showFeaturedBadge,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = GmsSpacing.Small, y = -GmsSpacing.Small),
            enter = scaleIn(animationSpec = FeaturedBadgeEnterSpring, initialScale = 0.4f) + fadeIn(),
            exit = scaleOut(targetScale = 0.4f) + fadeOut(),
        ) {
            RecommendationFeaturedBadge()
        }
    }

    if (supportInfoVisible) {
        RecommendationSupportInfoSheet(
            currentStatus = recommendation.supportStatus,
            onDismiss = { supportInfoVisible = false },
        )
    }
    if (applicationInfoVisible) {
        RecommendationApplicationInfoSheet(
            currentStatus = recommendation.applicationStatus,
            onDismiss = { applicationInfoVisible = false },
        )
    }
}
