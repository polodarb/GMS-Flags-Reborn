package ua.polodarb.gmsflags.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ua.polodarb.gmsflags.analytics.CrashReporter
import ua.polodarb.gmsflags.domain.navigation.RefreshNavigationFlags
import ua.polodarb.gmsflags.domain.onboarding.ObserveOnboardingCompletion
import ua.polodarb.gmsflags.domain.onboarding.RequestRootAccess
import ua.polodarb.gmsflags.domain.servermode.RefreshServerMode
import ua.polodarb.gmsflags.navigationflags.KEY_HIDE_GMS_INSIGHT
import ua.polodarb.gmsflags.remoteconfig.REMOTE_CONFIG_LOG_PREFIX
import ua.polodarb.gmsflags.servermode.KEY_OFFLINE_MODE

internal class AppStartupViewModel(
    observeOnboardingCompletion: ObserveOnboardingCompletion,
    private val requestRootAccess: RequestRootAccess,
    private val refreshServerMode: RefreshServerMode,
    private val hasCachedServerMode: () -> Boolean,
    private val refreshNavigationFlags: RefreshNavigationFlags,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val mutableState = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    val state: StateFlow<AppStartupState> = mutableState.asStateFlow()
    private var rootCheck: Job? = null

    init {
        viewModelScope.launch {
            if (hasCachedServerMode()) {
                launch { refreshQuietly(KEY_OFFLINE_MODE) { refreshServerMode() } }
            } else {
                withTimeoutOrNull(SERVER_MODE_REFRESH_TIMEOUT_MILLIS) {
                    refreshQuietly(KEY_OFFLINE_MODE) { refreshServerMode() }
                }
            }
            launch { refreshQuietly(KEY_HIDE_GMS_INSIGHT) { refreshNavigationFlags() } }
            observeOnboardingCompletion()
                .distinctUntilChanged()
                .collect { completed ->
                    if (completed) checkRootAccess()
                    else mutableState.value = AppStartupState.Onboarding
                }
        }
    }

    private suspend fun refreshQuietly(remoteConfigParameter: String, refresh: suspend () -> Unit) {
        try {
            refresh()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            crashReporter.log("$REMOTE_CONFIG_LOG_PREFIX $remoteConfigParameter startup refresh threw")
            crashReporter.recordException(error)
        }
    }

    fun retryRootAccess() {
        checkRootAccess()
    }

    private fun checkRootAccess() {
        rootCheck?.cancel()
        rootCheck = viewModelScope.launch {
            mutableState.value = AppStartupState.Loading
            mutableState.value = if (requestRootAccess().isSuccess) {
                AppStartupState.Ready
            } else {
                AppStartupState.RootUnavailable
            }
        }
    }

    private companion object {
        const val SERVER_MODE_REFRESH_TIMEOUT_MILLIS = 2_000L
    }
}
