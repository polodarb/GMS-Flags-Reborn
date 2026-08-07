package ua.polodarb.gmsflags.presentation.feature.apps.navigation

sealed class AppsScreenAction {
    data object OpenHookStatus : AppsScreenAction()

    data class OpenApplication(
        val androidPackageName: String,
        val applicationName: String,
        val flagPackageName: String,
        val availableFlagPackageNames: List<String>,
    ) : AppsScreenAction()
}
