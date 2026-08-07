package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.RecommendationDimensions
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.HomeInfoBlockUiModel

@Composable
internal fun HomeInfoBlockCard(
    infoBlock: HomeInfoBlockUiModel,
    modifier: Modifier = Modifier,
) {
    val appearance = infoBlock.type.appearance()
    val uriHandler = LocalUriHandler.current
    val link = infoBlock.externalLink
    val clickModifier = if (link != null) {
        Modifier.clickable { runCatching { uriHandler.openUri(link) } }
    } else {
        Modifier
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = appearance.containerColor,
        contentColor = appearance.contentColor,
    ) {
        Row(
            modifier = Modifier
                .then(clickModifier)
                .padding(GmsSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = appearance.icon,
                contentDescription = null,
                modifier = Modifier.size(RecommendationDimensions.WarningIconSize),
            )
            Text(
                text = infoBlock.message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (link != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(RecommendationDimensions.WarningIconSize),
                )
            }
        }
    }
}
