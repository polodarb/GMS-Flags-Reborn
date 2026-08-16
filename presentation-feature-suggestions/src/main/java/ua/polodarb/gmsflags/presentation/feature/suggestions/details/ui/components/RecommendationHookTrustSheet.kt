package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecommendationHookTrustSheet(
    currentStatus: HookTrustStatus,
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
        HookTrustSheetContent(currentStatus = currentStatus, onClose = hideSheet)
    }
}

@Composable
private fun HookTrustSheetContent(currentStatus: HookTrustStatus, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = GmsSpacing.ExtraLarge,
                end = GmsSpacing.ExtraLarge,
                bottom = GmsSpacing.Small,
            ),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
            Text(
                text = stringResource(R.string.suggestions_details_patch_trust_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.suggestions_details_patch_trust_sheet_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
            HookTrustStates.forEach { status ->
                HookTrustStatusRow(status = status, selected = status == currentStatus)
            }
        }
        if (currentStatus == HookTrustStatus.APP_UPDATE_REQUIRED) {
            val uriHandler = LocalUriHandler.current
            Button(
                onClick = { uriHandler.openUri(GMS_FLAGS_RELEASES_URL) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.suggestions_details_patch_trust_update_button))
            }
        }
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.suggestions_support_sheet_close))
        }
    }
}

@Preview(name = "Trust sheet · update required", showBackground = true, widthDp = 400)
@Composable
private fun HookTrustSheetUpdateRequiredPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            HookTrustSheetContent(currentStatus = HookTrustStatus.APP_UPDATE_REQUIRED, onClose = {})
        }
    }
}

@Preview(name = "Trust sheet · verified", showBackground = true, widthDp = 400)
@Composable
private fun HookTrustSheetVerifiedPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            HookTrustSheetContent(currentStatus = HookTrustStatus.VERIFIED, onClose = {})
        }
    }
}

@Composable
private fun HookTrustStatusRow(status: HookTrustStatus, selected: Boolean) {
    val appearance = status.appearance()
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) appearance.containerColor else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (selected) appearance.contentColor else MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
                shape = MaterialTheme.shapes.large,
                color = appearance.containerColor,
                contentColor = appearance.contentColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = appearance.icon, contentDescription = null)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(
                    text = stringResource(appearance.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(appearance.descriptionRes),
                    color = if (selected) appearance.contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private const val GMS_FLAGS_RELEASES_URL = "https://github.com/polodarb/GMS-Flags-Reborn/releases/latest"

private val HookTrustStates = listOf(
    HookTrustStatus.VERIFIED,
    HookTrustStatus.NOT_SIGNED,
    HookTrustStatus.CANNOT_VERIFY_IN_THIS_BUILD,
    HookTrustStatus.VERIFICATION_FAILED,
    HookTrustStatus.APP_UPDATE_REQUIRED,
)
