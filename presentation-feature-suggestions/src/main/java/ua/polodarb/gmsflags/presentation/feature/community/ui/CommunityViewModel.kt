package ua.polodarb.gmsflags.presentation.feature.community.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.domain.community.CommunityPackage
import ua.polodarb.gmsflags.data.repository.community.CommunityRepository

import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType

import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEffect
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEvent
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityState

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val flagDetailsRepository: FlagDetailsRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(CommunityState())
    val viewState: StateFlow<CommunityState> = _viewState.asStateFlow()

    private val _effect = MutableSharedFlow<CommunityEffect>()
    val effect: SharedFlow<CommunityEffect> = _effect.asSharedFlow()

    init {
        loadData()
    }

    fun setEvent(event: CommunityEvent) {
        when (event) {
            is CommunityEvent.SearchQueryChanged -> {
                _viewState.update { it.copy(searchQuery = event.query) }
            }
            is CommunityEvent.PackageSelected -> {
                viewModelScope.launch {
                    val isDismissed = communityRepository.isDisclaimerDismissed()
                    _viewState.update {
                        it.copy(
                            selectedPackage = event.packageItem,
                            isDisclaimerDismissed = isDismissed,
                            showDisclaimerDialog = !isDismissed,
                        )
                    }
                }
            }
            CommunityEvent.DismissDetails -> {
                _viewState.update { it.copy(selectedPackage = null, showDisclaimerDialog = false, showReportDialog = false) }
            }
            CommunityEvent.DismissDisclaimer -> {
                viewModelScope.launch {
                    communityRepository.setDisclaimerDismissed(true)
                    _viewState.update { it.copy(isDisclaimerDismissed = true, showDisclaimerDialog = false) }
                }
            }
            is CommunityEvent.ReportClicked -> {
                _viewState.update { it.copy(showReportDialog = true) }
            }
            is CommunityEvent.SubmitReport -> {
                val pkgId = _viewState.value.selectedPackage?.id ?: return
                viewModelScope.launch {
                    communityRepository.reportPackage(pkgId, event.reason)
                    _viewState.update { it.copy(showReportDialog = false) }
                    _effect.emit(CommunityEffect.ShowSnackbar("Report submitted"))
                }
            }
            is CommunityEvent.OpenSubmitDialog -> {
                _viewState.update {
                    it.copy(
                        showSubmitDialog = true,
                        submitPackageName = event.defaultPackageName,
                        submitFlags = event.initialFlags,
                    )
                }
            }
            CommunityEvent.CloseSubmitDialog -> {
                _viewState.update { it.copy(showSubmitDialog = false, submitTitle = "", submitDescription = "", submitFlags = emptyList()) }
            }
            is CommunityEvent.AddFlagToSubmit -> {
                val sanitizedFlag = CommunityFlagItem(
                    flagName = sanitizeInput(event.flagName),
                    valueType = sanitizeInput(event.valueType),
                    value = sanitizeInput(event.value),
                )
                _viewState.update { it.copy(submitFlags = it.submitFlags + sanitizedFlag) }
            }
            is CommunityEvent.RemoveFlagFromSubmit -> {
                val current = _viewState.value.submitFlags.toMutableList()
                if (event.index in current.indices) {
                    current.removeAt(event.index)
                    _viewState.update { it.copy(submitFlags = current) }
                }
            }
            is CommunityEvent.SubmitPackage -> {
                val title = sanitizeInput(event.title)
                val desc = sanitizeInput(event.description)
                val pkg = sanitizeInput(event.packageName)
                val flags = _viewState.value.submitFlags
                if (title.isBlank() || pkg.isBlank() || flags.isEmpty()) {
                    viewModelScope.launch { _effect.emit(CommunityEffect.ShowSnackbar("Title, package and flags are required")) }
                    return
                }
                viewModelScope.launch {
                    communityRepository.submitPackage(title, desc, pkg, flags)
                    _viewState.update { it.copy(showSubmitDialog = false, submitTitle = "", submitDescription = "", submitFlags = emptyList()) }
                    _effect.emit(CommunityEffect.ShowSnackbar("Package submitted to community"))
                    loadData()
                }
            }
            is CommunityEvent.InstallFlags -> {
                installFlagsSafely(event.packageItem)
            }
            CommunityEvent.Refresh -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            val packages = communityRepository.getPackages(_viewState.value.searchQuery)
            _viewState.update { it.copy(isLoading = false, packages = packages) }
        }
    }

    private fun installFlagsSafely(packageItem: CommunityPackage) {
        viewModelScope.launch {
            val sanitizedPkgName = sanitizeInput(packageItem.packageName)
            val overrides = packageItem.flags.map { flagItem ->
                val type = when (flagItem.valueType.lowercase()) {
                    "bool", "boolean" -> FlagType.Boolean
                    "int", "integer" -> FlagType.Integer
                    "float" -> FlagType.Float
                    else -> FlagType.String
                }
                FlagOverride(
                    name = sanitizeInput(flagItem.flagName),
                    type = type,
                    value = sanitizeInput(flagItem.value),
                )
            }
            val result = flagDetailsRepository.applyOverrides(
                androidPackageName = sanitizedPkgName,
                phenotypePackageName = sanitizedPkgName,
                overrides = overrides,
            )

            if (result.isSuccess) {
                _effect.emit(CommunityEffect.FlagsInstalled)
            } else {
                _effect.emit(CommunityEffect.ShowSnackbar("Failed to install flags"))
            }
        }
    }

    private fun sanitizeInput(input: String): String {
        return input.trim().replace("\u0000", "")
    }
}
