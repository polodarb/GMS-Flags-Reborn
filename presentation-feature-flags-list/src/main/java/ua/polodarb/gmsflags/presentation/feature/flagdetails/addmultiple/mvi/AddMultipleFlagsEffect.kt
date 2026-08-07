package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi

import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.error.UiError

sealed interface AddMultipleFlagsEffect : ViewSideEffect {
    data object NavigateBack : AddMultipleFlagsEffect
    data class ShowMessage(
        @param:StringRes val messageRes: Int,
        val type: UiMessageType = UiMessageType.Error,
    ) : AddMultipleFlagsEffect

    data class ShowError(val error: UiError) : AddMultipleFlagsEffect
}
