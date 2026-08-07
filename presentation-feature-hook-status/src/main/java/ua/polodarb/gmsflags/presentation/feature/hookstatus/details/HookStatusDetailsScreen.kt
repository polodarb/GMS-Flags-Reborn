package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.descriptionResource
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.titleResource

@Composable
fun HookStatusDetailsScreen(
    androidPackageName: String,
    onBack: () -> Unit,
) {
    val viewModel: HookStatusDetailsViewModel = koinViewModel(
        parameters = { parametersOf(androidPackageName) }
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = LocalGmsSnackbarHostState.current
    val launchFailed = stringResource(R.string.hook_status_launch_failed)
    val shareTitle = stringResource(R.string.hook_status_share_title)
    val overridesDeletedMessage = stringResource(R.string.hook_status_overrides_deleted)

    LifecycleResumeEffect(viewModel) {
        viewModel.setEvent(HookStatusDetailsEvent.Refresh)
        onPauseOrDispose { }
    }
    LaunchedEffect(viewModel, context) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HookStatusDetailsEffect.LaunchApplication -> {
                    val launchIntent = context.packageManager
                        .getLaunchIntentForPackage(effect.androidPackageName)
                    if (launchIntent == null) {
                        snackbarHostState.showSnackbar(
                            message = launchFailed,
                            type = UiMessageType.Error,
                        )
                    } else {
                        context.startActivity(launchIntent)
                    }
                }
                is HookStatusDetailsEffect.ShareDiagnostics -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                            putExtra(Intent.EXTRA_TEXT, effect.status.asShareText(context))
                        },
                        shareTitle,
                    )
                )
                is HookStatusDetailsEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.error.localizedDescription(context.resources),
                    type = UiMessageType.Error,
                )
                HookStatusDetailsEffect.OverridesDeleted -> snackbarHostState.showSnackbar(
                    message = overridesDeletedMessage,
                    type = UiMessageType.Success,
                )
            }
        }
    }

    HookStatusDetailsContent(
        state = state,
        onBack = onBack,
        onEvent = viewModel::setEvent,
    )
}

private fun HookApplicationStatus.asShareText(context: android.content.Context): String = buildString {
    appendLine(context.getString(R.string.hook_status_share_title))
    appendLine(context.getString(R.string.hook_status_share_application, applicationName))
    appendLine(context.getString(R.string.hook_status_share_package, androidPackageName))
    appendLine(context.getString(R.string.hook_status_share_health, health.name))
    appendLine(context.getString(R.string.hook_status_share_saved, currentOverrideCount))
    appendLine(context.getString(R.string.hook_status_share_loaded, loadedOverrideCount))
    appendLine(context.getString(R.string.hook_status_share_installed, installedStrategyCount))
    appendLine(context.getString(R.string.hook_status_share_applied, appliedCount))
    appendLine(context.getString(R.string.hook_status_share_consumed, consumedCount))
    compatibilityWarnings.forEach { warning ->
        appendLine(
            context.getString(
                R.string.hook_status_share_warning,
                context.getString(warning.titleResource()),
                context.getString(warning.descriptionResource()),
            )
        )
    }
    session?.let { session ->
        appendLine(context.getString(R.string.hook_status_share_process, session.processName))
        appendLine(context.getString(R.string.hook_status_share_version, session.versionCode))
        appendLine(context.getString(R.string.hook_status_share_session, session.startedAt))
        session.strategies.forEach { strategy ->
            appendLine(
                context.getString(
                    R.string.hook_status_share_strategy,
                    strategy.name,
                    strategy.state.name,
                    strategy.appliedCount,
                    strategy.consumedCount,
                )
            )
        }
    }
}
