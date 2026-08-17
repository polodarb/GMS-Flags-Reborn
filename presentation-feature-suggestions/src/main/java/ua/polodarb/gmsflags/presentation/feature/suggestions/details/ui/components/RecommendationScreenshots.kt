package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R

@Composable
internal fun RecommendationScreenshots(
    urls: List<String>,
    onScreenshotClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
    pagerState: PagerState = rememberPagerState(pageCount = urls::size),
) {
    val aspectRatios = remember(urls) { mutableStateMapOf<String, Float>() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
    ) {
        Text(
            text = stringResource(R.string.suggestions_details_screenshots),
            modifier = Modifier.padding(horizontal = horizontalPadding),
            style = MaterialTheme.typography.titleLarge,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val pageWidth = maxWidth * ScreenshotPageWidthFraction
            val viewportAspectRatio = urls.firstOrNull()?.let(aspectRatios::get)
                ?: FallbackAspectRatio
            val viewportHeight = pageWidth / viewportAspectRatio.coerceAtLeast(MinViewportAspectRatio)

            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(pageWidth),
                pageSpacing = GmsSpacing.Medium,
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(viewportHeight),
            ) { page ->
                val url = urls[page]
                val aspectRatio = aspectRatios[url]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = stringResource(
                            R.string.suggestions_details_screenshot_description,
                            page + 1,
                            urls.size,
                        ),
                        contentScale = ContentScale.Fit,
                        onSuccess = { state ->
                            val size = state.painter.intrinsicSize
                            if (size.width > 0f && size.height > 0f) {
                                aspectRatios[url] = size.width / size.height
                            }
                        },
                        modifier = Modifier
                            .then(
                                if (aspectRatio != null) {
                                    Modifier.aspectRatio(aspectRatio)
                                } else {
                                    Modifier.fillMaxSize()
                                },
                            )
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { onScreenshotClick(page) },
                    )
                }
            }
        }
        if (urls.size > 1) {
            Text(
                text = stringResource(
                    R.string.suggestions_details_screenshot_counter,
                    pagerState.currentPage + 1,
                    urls.size,
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val ScreenshotPageWidthFraction = 0.62f
private const val FallbackAspectRatio = 9f / 19.5f
private const val MinViewportAspectRatio = 9f / 22f
