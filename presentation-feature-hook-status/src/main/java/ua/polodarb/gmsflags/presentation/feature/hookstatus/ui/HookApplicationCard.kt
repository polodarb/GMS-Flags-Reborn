package ua.polodarb.gmsflags.presentation.feature.hookstatus.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.application.ApplicationIconProvider
import ua.polodarb.gmsflags.presentation.core.ui.application.GmsApplicationIcon
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R
import org.koin.compose.koinInject

@Composable
internal fun HookApplicationCard(
    status: HookApplicationStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconProvider: ApplicationIconProvider = koinInject()
    Card(
        onClick = rememberHapticClick(onClick = onClick),
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = GmsSpacing.None),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(GmsSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            ) {
                Text(
                    text = status.applicationName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val flagsText = stringResource(
                    R.string.hook_status_flags_count,
                    status.currentOverrideCount,
                )
                val sessionText = status.lastCheckedAt?.let { timestamp ->
                    stringResource(
                        R.string.hook_status_last_checked,
                        DateUtils.getRelativeTimeSpanString(timestamp),
                    )
                } ?: stringResource(R.string.hook_status_never_checked)
                Text(
                    text = stringResource(
                        R.string.hook_status_card_metadata,
                        flagsText,
                        sessionText,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GmsApplicationIcon(
                        packageName = status.androidPackageName,
                        applicationName = status.applicationName,
                        iconProvider = iconProvider,
                        modifier = Modifier
                            .size(HookStatusDimensions.StatusBadgeHeight)
                            .clip(MaterialTheme.shapes.small),
                    )
                    HookHealthBadge(
                        health = status.health,
                        showIcon = false,
                    )
                    if (status.compatibilityWarnings.isNotEmpty()) {
                        HookCompatibilityWarningBadge()
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
