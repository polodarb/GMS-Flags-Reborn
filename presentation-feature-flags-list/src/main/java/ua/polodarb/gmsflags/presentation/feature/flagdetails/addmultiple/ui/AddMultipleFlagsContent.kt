package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.components.AddMultipleFlagForm
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.components.AddMultipleFlagsPreview
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.components.AddMultipleSaveBar
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@Composable
internal fun AddMultipleFlagsContent(
    state: AddMultipleFlagsState,
    onEvent: (AddMultipleFlagsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(R.string.add_multiple_title),
                onBack = { onEvent(AddMultipleFlagsEvent.BackClicked) },
            )
        },
        bottomBar = {
            AddMultipleSaveBar(
                saving = state.saving,
                enabled = state.canSave,
                flagCount = state.previewFlags.size,
                onSave = { onEvent(AddMultipleFlagsEvent.SaveClicked) },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(top = GmsSpacing.Small),
        ) {
            GmsContentContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = adaptiveLayout.contentMaxWidth)
                    .align(Alignment.Center),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = GmsSpacing.Large),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
                ) {
                    AddMultipleFlagForm(state = state, onEvent = onEvent)
                    AddMultipleFlagsPreview(
                        flags = state.previewFlags,
                        invalidTokenCount = state.invalidTokenCount,
                    )
                }
            }
        }
    }
}
