package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ImportFlagsBottomBar(
    selectedCount: Int,
    applying: Boolean,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceBright)
            .navigationBarsPadding()
            .padding(adaptiveLayout.contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = rememberHapticClick(onClick = onApply),
            enabled = selectedCount > 0 && !applying,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = adaptiveLayout.listMaxWidth),
        ) {
            if (applying) {
                LoadingIndicator(
                    modifier = Modifier.size(GmsDimensions.SelectionIndicatorSize),
                )
            } else {
                Text(stringResource(R.string.import_flags_apply, selectedCount))
            }
        }
    }
}
