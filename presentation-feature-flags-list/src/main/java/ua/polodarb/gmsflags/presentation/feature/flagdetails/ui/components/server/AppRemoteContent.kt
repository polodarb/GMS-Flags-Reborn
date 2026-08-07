package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.server

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.domain.server.content.InfoBlockType
import ua.polodarb.gmsflags.domain.server.content.RecommendationSupportStatus
import ua.polodarb.gmsflags.domain.server.content.ServerInfoBlock
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.AppRemoteContentState

internal val AppRemoteContentState.hasVisibleContent: Boolean
    get() = when (this) {
        is AppRemoteContentState.Error -> true
        is AppRemoteContentState.Ready -> infoBlocks.isNotEmpty() || recommendations.isNotEmpty()
        AppRemoteContentState.Loading,
        AppRemoteContentState.Unavailable,
        -> false
    }

@Composable
internal fun AppRemoteContent(
    state: AppRemoteContentState,
    onRetry: () -> Unit,
    onRecommendationClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when (state) {
            is AppRemoteContentState.Error -> RemoteContentError(onRetry = onRetry)
            is AppRemoteContentState.Ready -> RemoteContentReady(
                infoBlocks = state.infoBlocks,
                recommendations = state.recommendations,
                onRecommendationClick = onRecommendationClick,
            )
            AppRemoteContentState.Loading,
            AppRemoteContentState.Unavailable,
            -> Unit
        }
    }
}

@Composable
private fun RemoteContentReady(
    infoBlocks: List<ServerInfoBlock>,
    recommendations: List<ServerRecommendationSummary>,
    onRecommendationClick: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large)) {
        infoBlocks.forEach { infoBlock -> InfoBlockCard(infoBlock) }
        if (recommendations.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium)) {
                Text(
                    text = stringResource(R.string.flag_details_recommendations),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                ) {
                    items(recommendations, key = { it.id }) { recommendation ->
                        RecommendationPreviewCard(
                            recommendation = recommendation,
                            onClick = { onRecommendationClick(recommendation.id) },
                            modifier = Modifier
                                .fillParentMaxWidth(0.82f)
                                .widthIn(max = LocalGmsAdaptiveLayout.current.recommendationCardMinWidth),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBlockCard(infoBlock: ServerInfoBlock) {
    val appearance = infoBlock.type.appearance()
    val uriHandler = LocalUriHandler.current
    val link = infoBlock.externalLink?.trim()?.takeIf(String::isNotEmpty)
    val clickModifier = if (link != null) {
        Modifier.clickable { runCatching { uriHandler.openUri(link) } }
    } else {
        Modifier
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = appearance.containerColor,
        contentColor = appearance.contentColor,
    ) {
        Row(
            modifier = Modifier
                .then(clickModifier)
                .padding(GmsSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(imageVector = appearance.icon, contentDescription = null)
            Text(
                text = infoBlock.message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (link != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun RecommendationPreviewCard(
    recommendation: ServerRecommendationSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            recommendation.description?.takeIf(String::isNotBlank)?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            Text(
                text = stringResource(recommendation.supportStatus.labelRes()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun RemoteContentError(onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.flag_details_online_content_unavailable),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
private fun InfoBlockType.appearance(): InfoBlockAppearance = when (this) {
    InfoBlockType.Info -> InfoBlockAppearance(
        Icons.Rounded.Info,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer,
    )
    InfoBlockType.Warning -> InfoBlockAppearance(
        Icons.Rounded.Warning,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
    )
    InfoBlockType.Success -> InfoBlockAppearance(
        Icons.Rounded.CheckCircle,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
    )
    InfoBlockType.Promo -> InfoBlockAppearance(
        Icons.Rounded.Lightbulb,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
    )
    InfoBlockType.Danger -> InfoBlockAppearance(
        Icons.Rounded.Error,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer,
    )
    InfoBlockType.Unknown -> InfoBlockAppearance(
        Icons.Rounded.Info,
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun RecommendationSupportStatus.labelRes(): Int = when (this) {
    RecommendationSupportStatus.Verified -> R.string.flag_details_support_verified
    RecommendationSupportStatus.Partial -> R.string.flag_details_support_partial
    RecommendationSupportStatus.Experimental -> R.string.flag_details_support_experimental
    RecommendationSupportStatus.Unknown -> R.string.flag_details_support_unknown
}

private data class InfoBlockAppearance(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)
