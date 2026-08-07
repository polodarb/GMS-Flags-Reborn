package ua.polodarb.gmsflags.presentation.feature.apps.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.apps.R
import ua.polodarb.gmsflags.presentation.feature.apps.ui.icon.ApplicationIcon
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.ApplicationUiModel
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.XposedScopeStatusUi
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsScopeWarningVisibility
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeWarningBadge

@Composable
internal fun ApplicationCard(
    application: ApplicationUiModel,
    onClick: () -> Unit,
    onScopeHelpClick: () -> Unit,
    onPairipHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = rememberHapticClick(onClick = onClick),
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GmsSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ApplicationIcon(
                packageName = application.androidPackageName,
                applicationName = application.name,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .size(GmsDimensions.ApplicationIconSize),
            )
            Spacer(Modifier.width(GmsSpacing.Large))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.size(GmsSpacing.Small))
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusColor = when (application.scopeStatus) {
                            XposedScopeStatusUi.Included -> MaterialTheme.colorScheme.primary
                            XposedScopeStatusUi.Excluded -> MaterialTheme.colorScheme.error
                            XposedScopeStatusUi.Unknown -> MaterialTheme.colorScheme.outline
                        }
                        Box(
                            modifier = Modifier
                                .size(GmsDimensions.StatusIndicatorSize)
                                .background(statusColor, CircleShape),
                        )
                        Spacer(Modifier.width(GmsSpacing.Small))
                        Text(
                            text = application.androidPackageName,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    GmsScopeWarningVisibility(
                        visible = application.scopeStatus == XposedScopeStatusUi.Excluded &&
                            !application.unsupported &&
                            !application.pairipIncompatible,
                    ) {
                        GmsXposedScopeWarningBadge(
                            label = stringResource(R.string.apps_scope_missing_badge),
                            onClick = onScopeHelpClick,
                        )
                    }
                    GmsScopeWarningVisibility(visible = application.unsupported) {
                        GmsXposedScopeWarningBadge(
                            label = stringResource(R.string.apps_unsupported_badge),
                            onClick = onClick,
                        )
                    }
                    GmsScopeWarningVisibility(
                        visible = application.pairipIncompatible && !application.unsupported,
                    ) {
                        GmsXposedScopeWarningBadge(
                            label = stringResource(R.string.apps_pairip_badge),
                            onClick = onPairipHelpClick,
                        )
                    }
                }
            }
        }
    }
}
