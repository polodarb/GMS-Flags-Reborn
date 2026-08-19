package ua.polodarb.gmsflags.presentation.feature.community.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplications
import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.domain.community.CommunityPackage
import ua.polodarb.gmsflags.domain.community.GetCommunityPackagesUseCase
import ua.polodarb.gmsflags.domain.community.SubmitCommunityPackageUseCase
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEffect
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEvent
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityState

import ua.polodarb.gmsflags.domain.community.ReportCommunityPackageUseCase

class CommunityViewModel(
    private val getCommunityPackages: GetCommunityPackagesUseCase,
    private val submitCommunityPackage: SubmitCommunityPackageUseCase,
    private val reportCommunityPackage: ReportCommunityPackageUseCase,
    private val applyFlagOverrides: ApplyFlagOverrides,
    private val getSupportedApplications: GetSupportedApplications,
) : BaseViewModel<CommunityEvent, CommunityState, CommunityEffect>() {

    override fun initialState() = CommunityState()

    init {
        loadPackages()
    }

    override fun handleEvent(event: CommunityEvent) {
        when (event) {
            CommunityEvent.Refresh -> loadPackages()
            CommunityEvent.SearchToggled -> {
                setState {
                    val nextVisible = !searchVisible
                    copy(
                        searchVisible = nextVisible,
                        searchQuery = if (!nextVisible) "" else searchQuery,
                    )
                }
                loadPackages()
            }
            is CommunityEvent.QueryChanged -> {
                setState { copy(searchQuery = event.query) }
                loadPackages()
            }
            is CommunityEvent.PackageSelected -> {
                setState {
                    copy(
                        selectedPackage = event.packageItem,
                        showDisclaimer = false,
                    )
                }
            }
            CommunityEvent.PackageSheetDismissed -> setState { copy(selectedPackage = null) }
            CommunityEvent.DisclaimerDismissed -> setState { copy(showDisclaimer = false) }
            CommunityEvent.OpenSubmitDialog -> setState { copy(showSubmitDialog = true) }
            CommunityEvent.SubmitDialogDismissed -> setState { copy(showSubmitDialog = false) }
            is CommunityEvent.InstallFlags -> installFlagsSafely(event.packageItem)
            is CommunityEvent.ReportPackage -> reportPackage(event.packageId, event.reason)
            is CommunityEvent.SubmitPackage -> submitPackage(
                title = event.title,
                description = event.description,
                packageName = event.packageName,
                flags = event.flags,
            )
            CommunityEvent.SettingsClicked -> setEffect { CommunityEffect.OpenSettings }
        }
    }

    private fun reportPackage(packageId: Long, reason: String) {
        viewModelScope.launch {
            val success = reportCommunityPackage(packageId, reason, null)
            if (success) {
                setEffect { CommunityEffect.ShowSnackbar("Report submitted") }
            } else {
                setEffect { CommunityEffect.ShowSnackbar("Failed to submit report") }
            }
        }
    }



    private fun submitPackage(
        title: String,
        description: String,
        packageName: String,
        flags: List<CommunityFlagItem>,
    ) {
        viewModelScope.launch {
            setState { copy(showSubmitDialog = false) }
            val success = submitCommunityPackage(
                title = title.trim(),
                description = description.trim(),
                packageName = packageName.trim(),
                flags = flags,
            )
            if (success) {
                setEffect { CommunityEffect.ShowSnackbar("Package submitted successfully") }
                loadPackages()
            } else {
                setEffect { CommunityEffect.ShowSnackbar("Failed to submit package") }
            }
        }
    }

    private fun loadPackages() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val query = viewState.value.searchQuery.ifBlank { null }
            val packages = getCommunityPackages(query)
            setState { copy(isLoading = false, packages = packages) }
        }
    }

    private fun installFlagsSafely(packageItem: CommunityPackage) {
        viewModelScope.launch {
            val phenotypePackage = packageItem.packageName.trim()
            val applications = getSupportedApplications().getOrDefault(emptyList())
            val matchedApp = applications.firstOrNull { app ->
                app.flagPackages.any { it.packageName == phenotypePackage }
            }
            val androidPackage = matchedApp?.androidPackageName ?: phenotypePackage

            val overrides = packageItem.flags.mapNotNull { flagItem ->
                val name = flagItem.flagName.trim()
                if (name.isEmpty()) return@mapNotNull null
                val type = when (flagItem.valueType.lowercase()) {
                    "bool", "boolean" -> FlagType.Boolean
                    "int", "integer" -> FlagType.Integer
                    "float" -> FlagType.Float
                    "string" -> FlagType.String
                    else -> FlagType.String
                }
                FlagOverride(
                    name = name,
                    type = type,
                    value = flagItem.value.trim(),
                )
            }

            val result = applyFlagOverrides(
                androidPackageName = androidPackage,
                phenotypePackageName = phenotypePackage,
                overrides = overrides,
            )

            if (result.isSuccess) {
                setEffect { CommunityEffect.FlagsInstalled }
            } else {
                setEffect { CommunityEffect.ShowSnackbar("Failed to install flags") }
            }
        }
    }
}
