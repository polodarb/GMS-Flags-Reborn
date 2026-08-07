package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi

import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent

sealed interface AddMultipleFlagsEvent : ViewEvent {
    data object BackClicked : AddMultipleFlagsEvent
    data class TypeSelected(val type: FlagType) : AddMultipleFlagsEvent
    data class InputChanged(val type: FlagType, val value: String) : AddMultipleFlagsEvent
    data class BooleanValueChanged(val enabled: Boolean) : AddMultipleFlagsEvent
    data object SaveClicked : AddMultipleFlagsEvent
}
