package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.GmsSnackbarDuration
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsEffect
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsEvent

@Composable
fun RecommendationDetailsScreen(
    recommendationId: Long,
    onBack: () -> Unit,
) {
    val viewModel: RecommendationDetailsViewModel = koinViewModel(
        key = "recommendation_$recommendationId",
        parameters = { parametersOf(recommendationId) },
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbar = LocalGmsSnackbarHostState.current
    val resources = LocalResources.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.setEvent(RecommendationDetailsEvent.ScopeRefresh)
        viewModel.setEvent(RecommendationDetailsEvent.ApplicationStatusRefresh)
    }

    LaunchedEffect(viewModel, uriHandler, clipboard) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RecommendationDetailsEffect.NavigateBack -> onBack()
                is RecommendationDetailsEffect.CopyFlagName -> {
                    clipboard.setClipEntry(
                        ClipEntry(
                            ClipData.newPlainText(
                                resources.getString(R.string.suggestions_details_clipboard_flag_name),
                                effect.name,
                            )
                        )
                    )
                    snackbar.showSnackbar(
                        message = resources.getString(R.string.suggestions_details_flag_name_copied),
                        type = UiMessageType.Success,
                    )
                }
                RecommendationDetailsEffect.Applied -> snackbar.showSnackbar(
                    message = resources.getString(R.string.suggestions_details_applied),
                    type = UiMessageType.Success,
                )
                RecommendationDetailsEffect.Disabled -> snackbar.showSnackbar(
                    message = resources.getString(R.string.suggestions_details_disabled),
                    type = UiMessageType.Success,
                )
                RecommendationDetailsEffect.DisabledUncertain -> snackbar.showSnackbar(
                    message = resources.getString(R.string.suggestions_details_disabled_uncertain),
                    type = UiMessageType.Warning,
                    duration = GmsSnackbarDuration.Long,
                )
                is RecommendationDetailsEffect.LaunchApplication -> {
                    if (!context.launchApplication(effect.androidPackageName)) {
                        snackbar.showSnackbar(
                            message = resources.getString(
                                R.string.suggestions_details_launch_unavailable
                            ),
                            type = UiMessageType.Error,
                        )
                    }
                }
                is RecommendationDetailsEffect.ShowError -> snackbar.showSnackbar(
                    message = effect.error.localizedDescription(resources),
                    type = UiMessageType.Error,
                )
                is RecommendationDetailsEffect.OpenExternalLink -> {
                    runCatching { uriHandler.openUri(effect.url) }.onFailure {
                        snackbar.showSnackbar(
                            message = resources.getString(
                                R.string.suggestions_details_link_unavailable
                            ),
                            type = UiMessageType.Error,
                        )
                    }
                }
            }
        }
    }

    RecommendationDetailsContent(state = state, onEvent = viewModel::setEvent)
}

private fun Context.launchApplication(packageName: String): Boolean {
    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
    return runCatching {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.isSuccess
}
