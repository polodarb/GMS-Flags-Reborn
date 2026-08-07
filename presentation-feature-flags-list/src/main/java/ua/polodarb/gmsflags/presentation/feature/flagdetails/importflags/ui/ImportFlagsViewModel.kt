package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document.FlagImportDocumentSource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.resolvePackageName
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.resolvedPackageName
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.PackageOverrideTarget
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.UnsupportedImportPackage
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.parser.GmsFlagsFileParser

internal class ImportFlagsViewModel(
    private val androidPackageName: String,
    private val currentPhenotypePackageName: String,
    supportedPhenotypePackageNames: List<String>,
    initialDocumentUri: String,
    private val documentSource: FlagImportDocumentSource,
    private val parser: GmsFlagsFileParser,
    private val applyOverrides: ApplyFlagOverrides,
    private val errorResolver: ErrorResolver,
    private val analytics: AnalyticsTracker,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseViewModel<ImportFlagsEvent, ImportFlagsState, ImportFlagsEffect>() {
    private val supportedPackages = buildSet {
        add(currentPhenotypePackageName)
        addAll(supportedPhenotypePackageNames)
    }.filter(String::isNotBlank).toSet()
    private var documentUri = initialDocumentUri
    private var loadJob: Job? = null

    override fun initialState() = ImportFlagsState(
        androidPackageName = androidPackageName,
        currentPhenotypePackageName = currentPhenotypePackageName,
        supportedPhenotypePackageNames = supportedPackages,
    )

    init {
        load(initialDocumentUri)
    }

    override fun handleEvent(event: ImportFlagsEvent) {
        when (event) {
            ImportFlagsEvent.BackClicked -> setEffect { ImportFlagsEffect.NavigateBack }
            ImportFlagsEvent.RetryClicked -> load(documentUri)
            ImportFlagsEvent.ChooseAnotherFileClicked -> {
                setEffect { ImportFlagsEffect.OpenDocumentPicker }
            }
            is ImportFlagsEvent.DocumentSelected -> load(event.documentUri)
            is ImportFlagsEvent.FlagSelectionChanged -> setState {
                copy(
                    selectedFlags = if (event.selected) {
                        selectedFlags + event.key
                    } else {
                        selectedFlags - event.key
                    }
                )
            }
            ImportFlagsEvent.SelectAllClicked -> setState {
                copy(selectedFlags = batch?.flags?.mapTo(mutableSetOf()) { it.key }.orEmpty())
            }
            ImportFlagsEvent.ClearSelectionClicked -> setState {
                copy(selectedFlags = emptySet())
            }
            ImportFlagsEvent.ApplyClicked -> applySelected()
            is ImportFlagsEvent.PackageOverrideRequested -> setState {
                copy(packageOverrideTarget = event.target)
            }
            ImportFlagsEvent.PackageOverrideDismissed -> setState {
                copy(packageOverrideTarget = null)
            }
            is ImportFlagsEvent.PackageOverrideSelected -> applyPackageOverride(event.packageName)
        }
    }

    private fun applyPackageOverride(packageName: String) {
        val target = viewState.value.packageOverrideTarget ?: return
        val batch = viewState.value.batch ?: return
        val updatedBatch = when (target) {
            PackageOverrideTarget.WholeBatch -> batch.copy(phenotypePackageName = packageName)
            is PackageOverrideTarget.SingleFlag -> batch.copy(
                flags = batch.flags.map { flag ->
                    if (flag.key == target.key) flag.copy(packageName = packageName) else flag
                },
            )
        }
        setState { copy(batch = updatedBatch, packageOverrideTarget = null) }
    }

    private fun load(uri: String) {
        if (uri.isBlank()) {
            showError(R.string.import_flags_invalid_file)
            return
        }
        documentUri = uri
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            setState {
                copy(
                    loading = true,
                    applying = false,
                    batch = null,
                    selectedFlags = emptySet(),
                    unsupportedPackage = null,
                    errorMessageRes = null,
                )
            }
            try {
                val document = documentSource.read(uri)
                val parsedRaw = withContext(backgroundDispatcher) { parser.parse(document.content) }
                val matchedPackageName = resolvePackageName(parsedRaw.phenotypePackageName, supportedPackages)
                if (matchedPackageName == null) {
                    setState {
                        copy(
                            loading = false,
                            displayName = document.displayName,
                            unsupportedPackage = UnsupportedImportPackage(
                                parsedRaw.phenotypePackageName
                            ),
                        )
                    }
                    return@launch
                }
                val parsed = parsedRaw.copy(phenotypePackageName = matchedPackageName)
                setState {
                    copy(
                        loading = false,
                        displayName = document.displayName,
                        batch = parsed,
                        selectedFlags = parsed.flags.mapTo(mutableSetOf()) { it.key },
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Unable to read flag import document", error)
                showError(R.string.import_flags_invalid_file)
            }
        }
    }

    private fun applySelected() {
        val state = viewState.value
        val batch = state.batch ?: return
        if (state.applying) return
        val grouped = batch.flags
            .filter { it.key in state.selectedFlags }
            .mapNotNull { flag ->
                val resolved = flag.resolvedPackageName(batch.phenotypePackageName, supportedPackages)
                resolved?.let { it to flag.toOverride() }
            }
            .groupBy({ it.first }, { it.second })
        if (grouped.isEmpty()) {
            showMessage(R.string.import_flags_select_at_least_one)
            return
        }

        setState { copy(applying = true, errorMessageRes = null) }
        viewModelScope.launch {
            val results: List<Pair<String, Result<Unit>>> = coroutineScope {
                grouped.map { (packageName, overrides) ->
                    async { packageName to applyOverrides(androidPackageName, packageName, overrides) }
                }.awaitAll()
            }
            val failure = results.firstOrNull { (_, result) -> result.isFailure }
            if (failure != null) {
                setState { copy(applying = false) }
                setEffect {
                    ImportFlagsEffect.ShowError(
                        errorResolver.resolve(failure.second.exceptionOrNull()!!)
                    )
                }
                return@launch
            }
            results.forEach { (packageName, _) ->
                analytics.track(
                    AnalyticsEvent.flagsBatchApplied(packageName, grouped.getValue(packageName).size)
                )
            }
            setEffect { ImportFlagsEffect.ImportCompleted(batch.phenotypePackageName) }
        }
    }

    private fun showError(@StringRes messageRes: Int) {
        setState {
            copy(
                loading = false,
                applying = false,
                batch = null,
                selectedFlags = emptySet(),
                unsupportedPackage = null,
                errorMessageRes = messageRes,
            )
        }
    }

    private fun showMessage(@StringRes messageRes: Int) {
        setEffect { ImportFlagsEffect.ShowMessage(messageRes) }
    }

    private companion object {
        const val TAG = "FlagImport"
    }
}
