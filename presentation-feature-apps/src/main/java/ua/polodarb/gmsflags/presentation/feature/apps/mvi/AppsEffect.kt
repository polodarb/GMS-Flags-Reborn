package ua.polodarb.gmsflags.presentation.feature.apps.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect

sealed class AppsEffect : ViewSideEffect {
    data class OpenApplication(
        val androidPackageName: String,
        val applicationName: String,
        val flagPackageName: String,
        val availableFlagPackageNames: List<String>,
    ) : AppsEffect()

    data object OpenHookStatus : AppsEffect()
}
