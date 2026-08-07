package ua.polodarb.gmsflags.startup

internal sealed interface AppStartupState {
    data object Loading : AppStartupState
    data object Onboarding : AppStartupState
    data object Ready : AppStartupState
    data object RootUnavailable : AppStartupState
}
