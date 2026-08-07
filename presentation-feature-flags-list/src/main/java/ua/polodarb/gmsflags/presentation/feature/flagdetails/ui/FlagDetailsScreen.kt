package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.navigation.FlagDetailsScreenAction
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.effect.FlagDetailsEffectHandler

@Composable
fun FlagDetailsScreen(
    androidPackageName: String,
    applicationName: String,
    phenotypePackageName: String,
    availablePhenotypePackageNames: List<String>,
    onAction: (FlagDetailsScreenAction) -> Unit,
) {
    val viewModel: FlagDetailsViewModel = koinViewModel(
        key = "flag-details:$androidPackageName:$phenotypePackageName",
        parameters = {
            parametersOf(
                androidPackageName,
                applicationName,
                phenotypePackageName,
                availablePhenotypePackageNames,
            )
        },
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.setEvent(FlagDetailsEvent.ScopeRefresh)
    }
    val importFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            onAction(
                FlagDetailsScreenAction.ImportFlags(
                    androidPackageName = state.androidPackageName,
                    applicationName = state.applicationName,
                    currentPhenotypePackageName = state.phenotypePackageName,
                    supportedPhenotypePackageNames = state.availablePhenotypePackageNames,
                    documentUri = it.toString(),
                )
            )
        }
    }

    FlagDetailsEffectHandler(
        effects = viewModel.effect,
        onAction = onAction,
        onOpenImportFilePicker = { importFilePicker.launch(IMPORT_MIME_TYPES) },
    )

    FlagDetailsContent(state = state, onEvent = viewModel::setEvent)
}

private val IMPORT_MIME_TYPES = arrayOf(
    "application/gmsflags",
    "application/xml",
    "text/xml",
    "application/octet-stream",
    "text/plain",
)
