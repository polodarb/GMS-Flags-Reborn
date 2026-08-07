package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi

import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.error.UiError

sealed interface ImportFlagsEffect : ViewSideEffect {
    data object NavigateBack : ImportFlagsEffect
    data object OpenDocumentPicker : ImportFlagsEffect
    data class ImportCompleted(val phenotypePackageName: String) : ImportFlagsEffect
    data class ShowMessage(
        @param:StringRes val messageRes: Int,
        val type: UiMessageType = UiMessageType.Error,
    ) : ImportFlagsEffect

    data class ShowError(val error: UiError) : ImportFlagsEffect
}
