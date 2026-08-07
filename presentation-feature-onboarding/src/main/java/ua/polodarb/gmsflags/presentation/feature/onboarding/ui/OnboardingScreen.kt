package ua.polodarb.gmsflags.presentation.feature.onboarding.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.presentation.feature.onboarding.OnboardingViewModel
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingEffect
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingEvent
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep

@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.setEvent(OnboardingEvent.NotificationPermissionResult(granted))
        },
    )

    BackHandler(enabled = state.step != OnboardingStep.Welcome) {
        viewModel.setEvent(OnboardingEvent.Back)
    }

    LaunchedEffect(viewModel, context) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingEffect.RequestNotificationPermission -> {
                    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED
                    if (permissionGranted) {
                        viewModel.setEvent(OnboardingEvent.NotificationPermissionResult(true))
                    } else {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    OnboardingContent(state = state, onEvent = viewModel::setEvent)
}
