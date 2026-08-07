package ua.polodarb.gmsflags.presentation.feature.apps.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.apps.R
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.ApplicationUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UnsupportedAppSheet(
    application: ApplicationUiModel,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val hideSheet: () -> Unit = {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
        Unit
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Small,
                ),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                    Text(
                        text = stringResource(R.string.apps_unsupported_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = application.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            val intro = application.unsupportedFromVersion?.let { version ->
                stringResource(R.string.apps_unsupported_sheet_body_version, version)
            } ?: stringResource(R.string.apps_unsupported_sheet_body)
            Text(
                text = intro,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )

            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                UnsupportedInfoRow(
                    icon = Icons.Rounded.Block,
                    title = stringResource(R.string.apps_unsupported_sheet_point_paused_title),
                    description = stringResource(R.string.apps_unsupported_sheet_point_paused_desc),
                )
                UnsupportedInfoRow(
                    icon = Icons.Rounded.Update,
                    title = stringResource(R.string.apps_unsupported_sheet_point_fix_title),
                    description = stringResource(R.string.apps_unsupported_sheet_point_fix_desc),
                )
            }

            Button(
                onClick = hideSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GmsDimensions.PrimaryActionHeight),
            ) {
                Text(stringResource(R.string.apps_unsupported_sheet_action))
            }
        }
    }
}

@Composable
private fun UnsupportedInfoRow(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
