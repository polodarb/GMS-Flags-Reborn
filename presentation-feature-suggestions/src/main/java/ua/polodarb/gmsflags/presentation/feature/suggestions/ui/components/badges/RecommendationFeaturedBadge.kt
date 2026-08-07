package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges

import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.RecommendationDimensions
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.feature.suggestions.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RecommendationFeaturedBadge(modifier: Modifier = Modifier) {
    val shape = MaterialShapes.Cookie7Sided.toShape()
    Box(
        modifier = modifier
            .size(RecommendationDimensions.PinnedBadgeSize)
            .clip(shape)
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = stringResource(R.string.suggestions_featured_badge_description),
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(RecommendationDimensions.PinnedIconSize),
        )
    }
}
