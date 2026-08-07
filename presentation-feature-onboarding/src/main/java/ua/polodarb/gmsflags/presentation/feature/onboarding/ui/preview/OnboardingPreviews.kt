package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingState
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.OnboardingContent

@Preview(name = "Welcome", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WelcomePreview() = OnboardingPreview(OnboardingState())

@Preview(name = "Root denied", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RootDeniedPreview() = OnboardingPreview(
    OnboardingState(step = OnboardingStep.RootAccess, rootRequestFailed = true)
)

@Preview(name = "Notifications", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NotificationsPreview() = OnboardingPreview(
    OnboardingState(step = OnboardingStep.Notifications)
)

@Preview(
    name = "Notifications dark",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun NotificationsDarkPreview() = GMSFlags20Theme(
    darkTheme = true,
    dynamicColor = false,
) {
    OnboardingContent(
        state = OnboardingState(
            step = OnboardingStep.Notifications,
            notificationRequestDenied = true,
        ),
        onEvent = {},
    )
}

@Composable
private fun OnboardingPreview(state: OnboardingState) {
    GMSFlags20Theme(dynamicColor = false) {
        OnboardingContent(state = state, onEvent = {})
    }
}
