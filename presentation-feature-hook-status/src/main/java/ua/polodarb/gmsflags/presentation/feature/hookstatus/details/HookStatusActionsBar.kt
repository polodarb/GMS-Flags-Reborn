package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.HookStatusDimensions

@Composable
internal fun HookStatusActionsBar(
    restarting: Boolean,
    hasOverrides: Boolean,
    onEvent: (HookStatusDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = adaptiveLayout.contentMaxWidth)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = adaptiveLayout.contentPadding,
                    vertical = GmsSpacing.Medium,
                ),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = rememberHapticClick { onEvent(HookStatusDetailsEvent.RestartAndCheck) },
                enabled = !restarting,
                modifier = Modifier
                    .weight(1f)
                    .height(HookStatusDimensions.ActionButtonHeight),
            ) {
                Icon(Icons.Outlined.Sync, contentDescription = null)
                Text(
                    text = stringResource(
                        if (restarting) R.string.hook_status_checking
                        else R.string.hook_status_check_compatibility
                    ),
                    modifier = Modifier.padding(start = GmsSpacing.Small),
                )
            }
            OutlinedIconButton(
                onClick = rememberHapticClick { onEvent(HookStatusDetailsEvent.ShareDiagnostics) },
                modifier = Modifier.size(HookStatusDimensions.ActionButtonHeight),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.hook_status_share),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (hasOverrides) {
                OutlinedIconButton(
                    onClick = rememberHapticClick {
                        onEvent(HookStatusDetailsEvent.DeleteOverridesClicked)
                    },
                    modifier = Modifier.size(HookStatusDimensions.ActionButtonHeight),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = stringResource(R.string.hook_status_delete_overrides),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
