package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.error.UiError

sealed interface FlagDetailsEffect : ViewSideEffect {
    data object NavigateBack : FlagDetailsEffect
    data class CopyPackageName(val packageName: String) : FlagDetailsEffect
    data class CopyFlagNames(val names: List<String>) : FlagDetailsEffect
    data class OpenAddMultiple(
        val androidPackageName: String,
        val phenotypePackageName: String,
    ) : FlagDetailsEffect

    data object OpenImportFilePicker : FlagDetailsEffect
    data class OpenRecommendation(val id: Long) : FlagDetailsEffect

    data class LaunchApplication(val androidPackageName: String) : FlagDetailsEffect
    data class OpenAppSettings(val androidPackageName: String) : FlagDetailsEffect
    data class ShareFlags(
        val fileName: String,
        val content: String,
    ) : FlagDetailsEffect

    data class ReportFlags(
        val packageName: String,
        val description: String,
        val flags: String,
    ) : FlagDetailsEffect

    data class ShowMessage(
        @param:StringRes val messageRes: Int,
        val type: UiMessageType,
        val longDuration: Boolean = false,
    ) : FlagDetailsEffect

    data class ShowError(val error: UiError) : FlagDetailsEffect
}
