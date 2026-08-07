package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.parser.FlagBatchParser
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

internal class AddMultipleFlagsViewModel(
    private val androidPackageName: String,
    private val phenotypePackageName: String,
    private val applyOverrides: ApplyFlagOverrides,
    private val parser: FlagBatchParser,
    private val errorResolver: ErrorResolver,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<AddMultipleFlagsEvent, AddMultipleFlagsState, AddMultipleFlagsEffect>() {
    override fun initialState() = AddMultipleFlagsState(
        androidPackageName = androidPackageName,
        phenotypePackageName = phenotypePackageName,
    )

    override fun handleEvent(event: AddMultipleFlagsEvent) {
        when (event) {
            AddMultipleFlagsEvent.BackClicked -> setEffect { AddMultipleFlagsEffect.NavigateBack }
            is AddMultipleFlagsEvent.TypeSelected -> setState { copy(selectedType = event.type) }
            is AddMultipleFlagsEvent.InputChanged -> setState {
                copy(inputs = inputs + (event.type to event.value)).withPreview()
            }
            is AddMultipleFlagsEvent.BooleanValueChanged -> setState {
                copy(booleanValue = event.enabled).withPreview()
            }
            AddMultipleFlagsEvent.SaveClicked -> save()
        }
    }

    private fun save() {
        if (viewState.value.saving) return
        val overrides = runCatching { parser.parse(viewState.value) }.getOrElse {
            setEffect { AddMultipleFlagsEffect.ShowMessage(R.string.message_invalid_flags) }
            return
        }
        if (overrides.isEmpty()) {
            setEffect {
                AddMultipleFlagsEffect.ShowMessage(R.string.message_add_at_least_one_flag)
            }
            return
        }

        setState { copy(saving = true) }
        viewModelScope.launch {
            runCatching {
                applyOverrides(androidPackageName, phenotypePackageName, overrides).getOrThrow()
            }.fold(
                onSuccess = {
                    analytics.track(
                        AnalyticsEvent.flagsBatchApplied(phenotypePackageName, overrides.size)
                    )
                    setEffect { AddMultipleFlagsEffect.NavigateBack }
                },
                onFailure = { error ->
                    setState { copy(saving = false) }
                    setEffect {
                        AddMultipleFlagsEffect.ShowError(errorResolver.resolve(error))
                    }
                },
            )
        }
    }

    private fun AddMultipleFlagsState.withPreview(): AddMultipleFlagsState =
        parser.preview(this).let { preview ->
            copy(
                previewFlags = preview.overrides,
                invalidTokenCount = preview.invalidTokenCount,
            )
        }
}
