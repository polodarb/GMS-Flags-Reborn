package ua.polodarb.gmsflags.presentation.feature.insight.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.insight.R

@Composable
internal fun InsightHero(installed: Boolean, onActionClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge)) {
            InsightFlagMarquee(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = GmsSpacing.ExtraLarge),
            )
            Column(
                modifier = Modifier.padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Huge,
                ),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InsightLogoBadge()
                    Column {
                        Text(
                            text = stringResource(R.string.insight_app_name),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                        Text(
                            text = stringResource(R.string.insight_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.insight_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = rememberHapticClick(onClick = onActionClick),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GmsDimensions.PrimaryActionHeight),
                ) {
                    if (installed) {
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Icon(
                            GooglePlaystoreLogo,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text(
                        text = stringResource(
                            if (installed) R.string.insight_open_app else R.string.insight_open_play_store
                        ),
                        modifier = Modifier.padding(start = GmsSpacing.Small),
                    )
                }
            }
        }
    }
}
