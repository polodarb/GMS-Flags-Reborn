package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplications
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document.FlagImportDocumentSource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportTarget
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.parser.GmsFlagsFileParser

internal class ExternalImportViewModel(
    private val documentUri: String,
    private val documentSource: FlagImportDocumentSource,
    private val parser: GmsFlagsFileParser,
    private val getSupportedApplications: GetSupportedApplications,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseViewModel<ExternalImportEvent, ExternalImportState, ExternalImportEffect>() {
    override fun initialState() = ExternalImportState()

    init {
        load()
    }

    override fun handleEvent(event: ExternalImportEvent) {
        when (event) {
            ExternalImportEvent.BackClicked -> setEffect { ExternalImportEffect.NavigateBack }
            ExternalImportEvent.RetryClicked -> load()
            is ExternalImportEvent.TargetSelected -> {
                viewState.value.targets
                    .firstOrNull { it.androidPackageName == event.androidPackageName }
                    ?.let { target ->
                        setEffect { ExternalImportEffect.OpenImport(target) }
                    }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState {
                copy(
                    loading = true,
                    targets = emptyList(),
                    unsupportedPackageName = null,
                    errorMessageRes = null,
                )
            }
            try {
                val document = documentSource.read(documentUri)
                val batch = withContext(backgroundDispatcher) { parser.parse(document.content) }
                val applications = getSupportedApplications().getOrThrow()
                val targets = applications.mapNotNull { application ->
                    val matchedFlagPackage = application.flagPackages.firstOrNull {
                        it.packageName == batch.phenotypePackageName
                    } ?: application.flagPackages.firstOrNull {
                        it.packageName.substringAfterLast('#') == batch.phenotypePackageName
                    } ?: return@mapNotNull null
                    ExternalImportTarget(
                        androidPackageName = application.androidPackageName,
                        applicationName = application.name,
                        phenotypePackageName = matchedFlagPackage.packageName,
                        supportedPhenotypePackageNames = application.flagPackages
                            .map { it.packageName },
                    )
                }
                setState {
                    copy(
                        loading = false,
                        displayName = document.displayName,
                        phenotypePackageName = batch.phenotypePackageName,
                        targets = targets,
                        unsupportedPackageName = batch.phenotypePackageName
                            .takeIf { targets.isEmpty() },
                    )
                }
                if (targets.size == 1) {
                    setEffect { ExternalImportEffect.OpenImport(targets.single()) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Unable to resolve external flag import", error)
                setState {
                    copy(
                        loading = false,
                        errorMessageRes = R.string.import_flags_invalid_file,
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "ExternalFlagImport"
    }
}
