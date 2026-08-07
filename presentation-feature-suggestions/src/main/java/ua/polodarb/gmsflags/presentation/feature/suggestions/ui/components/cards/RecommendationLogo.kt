package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards

import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.RecommendationDimensions
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.Dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.theme.GmsNestedCardShape

@Composable
internal fun RecommendationLogo(
    logoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = RecommendationDimensions.LogoSize,
    contentPadding: Dp = GmsSpacing.Small,
) {
    var loaded by remember(logoUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(GmsNestedCardShape)
            .background(Color.White)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (!loaded) {
            Icon(
                imageVector = Icons.Outlined.TipsAndUpdates,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                onSuccess = { loaded = true },
                onError = { loaded = false },
            )
        }
    }
}
