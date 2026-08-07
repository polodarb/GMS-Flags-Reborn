package ua.polodarb.gmsflags.presentation.feature.settings.faq

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ua.polodarb.gmsflags.domain.server.content.FaqEntry
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyContent
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.feature.settings.R

@Composable
internal fun FaqContent(
    state: FaqState,
    onBack: () -> Unit,
    onEvent: (FaqEvent) -> Unit,
) {
    val layout = LocalGmsAdaptiveLayout.current
    val contentState = when {
        state.loading -> ContentState.Loading
        state.error != null && state.entries.isEmpty() -> ContentState.Error(state.error)
        else -> ContentState.Content(state.entries)
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(R.string.settings_faq),
                onBack = onBack,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
            contentAlignment = Alignment.Center,
        ) {
            GmsContentContainer(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = contentState,
                    contentKey = { it::class },
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                    },
                    label = "faq-state",
                ) { current ->
                    when (current) {
                        ContentState.Loading -> GmsLoadingIndicator(Modifier.fillMaxSize())
                        is ContentState.Error -> GmsErrorContent(
                            error = current.error,
                            onRetry = { onEvent(FaqEvent.Retry) },
                        )
                        is ContentState.Content -> if (current.entries.isEmpty()) {
                            GmsEmptyContent(title = stringResource(R.string.settings_faq_empty))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = layout.contentPadding,
                                    end = layout.contentPadding,
                                    top = layout.contentPadding,
                                    bottom = contentPadding.calculateBottomPadding() +
                                        GmsSpacing.Large,
                                ),
                                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                            ) {
                                items(current.entries, key = { it.id }) { entry ->
                                    FaqItemCard(entry = entry)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqItemCard(entry: FaqEntry) {
    var expanded by remember(entry.id) { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "faqChevron",
    )
    Surface(
        onClick = rememberHapticClick { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GmsSpacing.Large)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.question,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (expanded) {
                Text(
                    text = entry.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private sealed interface ContentState {
    data object Loading : ContentState
    data class Error(val error: UiError) : ContentState
    data class Content(val entries: List<FaqEntry>) : ContentState
}
