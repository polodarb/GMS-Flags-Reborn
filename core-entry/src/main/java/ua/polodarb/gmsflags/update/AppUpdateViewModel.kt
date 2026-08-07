package ua.polodarb.gmsflags.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy
import ua.polodarb.gmsflags.domain.update.AppUpdateType
import ua.polodarb.gmsflags.domain.update.DismissSoftAppUpdate
import ua.polodarb.gmsflags.domain.update.GetAppUpdatePolicy

class AppUpdateViewModel(
    private val getAppUpdatePolicy: GetAppUpdatePolicy,
    private val dismissSoftAppUpdate: DismissSoftAppUpdate,
) : ViewModel() {
    private val mutablePolicy = MutableStateFlow<AppUpdatePolicy?>(null)
    val policy: StateFlow<AppUpdatePolicy?> = mutablePolicy.asStateFlow()

    init {
        viewModelScope.launch {
            mutablePolicy.value = runCatching { getAppUpdatePolicy() }.getOrNull()
        }
    }

    /** Ignored for FORCE — the sheet stays until the user updates. */
    fun dismiss() {
        val current = mutablePolicy.value ?: return
        if (current.type == AppUpdateType.FORCE) return
        mutablePolicy.value = null
        viewModelScope.launch {
            dismissSoftAppUpdate(current.policyId, current.softCooldownHours)
        }
    }
}
