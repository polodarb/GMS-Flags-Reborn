package ua.polodarb.gmsflags.presentation.feature.insight.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

private val BadgeSize = 64.dp
private val LogoSize = 36.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InsightLogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(BadgeSize)
            .clip(MaterialShapes.Cookie9Sided.toShape())
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            IcLogo,
            contentDescription = null,
            modifier = Modifier.size(LogoSize),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
