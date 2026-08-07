package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.pluralStringResource
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AddMultipleSaveBar(
    saving: Boolean,
    enabled: Boolean,
    flagCount: Int,
    onSave: () -> Unit,
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
            onClick = rememberHapticClick(onClick = onSave),
            enabled = enabled,
            modifier = Modifier
                .widthIn(max = adaptiveLayout.listMaxWidth)
                .fillMaxWidth()
                .height(GmsDimensions.PrimaryActionHeight),
        ) {
            if (saving) {
                LoadingIndicator(
                    modifier = Modifier.size(GmsDimensions.SelectionIndicatorSize)
                )
            } else {
                Text(
                    if (flagCount == 0) {
                        stringResource(R.string.add_multiple_save)
                    } else {
                        pluralStringResource(
                            R.plurals.add_multiple_save_count,
                            flagCount,
                            flagCount,
                        )
                    }
                )
            }
        }
    }
}
