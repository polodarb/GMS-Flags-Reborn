package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import kotlin.coroutines.cancellation.CancellationException

@Composable
internal fun FullscreenRecommendationGallery(
    urls: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(urls.indices),
        pageCount = urls::size,
    )

    val dismissProgress = remember { Animatable(0f) }
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { backEvent -> dismissProgress.snapTo(backEvent.progress) }
            dismissProgress.snapTo(0f)
            onDismiss()
        } catch (cancellation: CancellationException) {
            dismissProgress.animateTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val shrink = 1f - 0.1f * dismissProgress.value
                    scaleX = shrink
                    scaleY = shrink
                },
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = stringResource(
                    R.string.suggestions_details_screenshot_description,
                    page + 1,
                    urls.size,
                ),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(GmsSpacing.Small)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape),
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(
                    R.string.suggestions_details_close_screenshot,
                ),
                tint = Color.White,
            )
        }

        if (urls.size > 1) {
            Text(
                text = stringResource(
                    R.string.suggestions_details_screenshot_counter,
                    pagerState.currentPage + 1,
                    urls.size,
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(GmsSpacing.Medium)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    .padding(horizontal = GmsSpacing.Medium, vertical = GmsSpacing.ExtraSmall),
            )
        }
    }
}
