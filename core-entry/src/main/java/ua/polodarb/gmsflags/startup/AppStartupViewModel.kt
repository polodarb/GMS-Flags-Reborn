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
import ua.polodarb.gmsflags.domain.onboarding.ObserveOnboardingCompletion
import ua.polodarb.gmsflags.domain.onboarding.RequestRootAccess
import ua.polodarb.gmsflags.domain.servermode.RefreshServerMode

internal class AppStartupViewModel(
    observeOnboardingCompletion: ObserveOnboardingCompletion,
    private val requestRootAccess: RequestRootAccess,
    private val refreshServerMode: RefreshServerMode,
    private val hasCachedServerMode: () -> Boolean,
) : ViewModel() {
    private val mutableState = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    val state: StateFlow<AppStartupState> = mutableState.asStateFlow()
    private var rootCheck: Job? = null

    init {
        viewModelScope.launch {
            if (hasCachedServerMode()) {
                launch { refreshServerModeQuietly() }
            } else {
                withTimeoutOrNull(SERVER_MODE_REFRESH_TIMEOUT_MILLIS) {
                    refreshServerModeQuietly()
                }
            }
            observeOnboardingCompletion()
                .distinctUntilChanged()
                .collect { completed ->
                    if (completed) checkRootAccess()
                    else mutableState.value = AppStartupState.Onboarding
                }
        }
    }

    private suspend fun refreshServerModeQuietly() {
        try {
            refreshServerMode()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Unit
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
