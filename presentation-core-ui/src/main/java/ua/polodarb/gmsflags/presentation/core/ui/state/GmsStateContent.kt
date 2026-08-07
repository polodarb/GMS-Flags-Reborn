package ua.polodarb.gmsflags.presentation.core.ui.state

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.ui.R
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
fun GmsEmptyContent(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    GmsStateContent(
        title = title,
        description = description,
        modifier = modifier,
    )
}

@Composable
fun GmsErrorContent(
    error: UiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = error.toCopy()
    GmsStateContent(
        title = stringResource(copy.titleRes),
        description = stringResource(copy.descriptionRes),
        actionLabel = stringResource(R.string.action_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
private fun GmsStateContent(
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(GmsSpacing.ExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = GmsDimensions.StateContentMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                imageVector = gmsStateIllustration(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = GmsDimensions.StateIllustrationMaxWidth)
                    .aspectRatio(STATE_ILLUSTRATION_ASPECT_RATIO),
            )
            Spacer(Modifier.height(GmsSpacing.ExtraLarge))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            description?.takeIf(String::isNotBlank)?.let {
                Spacer(Modifier.height(GmsSpacing.Small))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            if (!actionLabel.isNullOrBlank() && onAction != null) {
                Spacer(Modifier.height(GmsSpacing.Large))
                FilledTonalButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private const val STATE_ILLUSTRATION_ASPECT_RATIO = 900f / 322f
