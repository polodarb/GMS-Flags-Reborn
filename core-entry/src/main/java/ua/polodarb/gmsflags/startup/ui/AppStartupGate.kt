package ua.polodarb.gmsflags.startup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.OnboardingScreen
import ua.polodarb.gmsflags.startup.AppStartupState

@Composable
internal fun AppStartupGate(
    state: AppStartupState,
    onRetryRoot: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceBright),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            AppStartupState.Loading -> GmsLoadingIndicator()
            AppStartupState.Onboarding -> OnboardingScreen()
            AppStartupState.Ready -> content()
            AppStartupState.RootUnavailable -> GmsErrorContent(
                error = UiError.RootUnavailable,
                onRetry = onRetryRoot,
            )
        }
    }
}
