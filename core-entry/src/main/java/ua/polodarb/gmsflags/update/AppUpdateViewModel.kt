package ua.polodarb.gmsflags.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy
import ua.polodarb.gmsflags.domain.update.AppUpdateType
import ua.polodarb.gmsflags.domain.update.DismissSoftAppUpdate
import ua.polodarb.gmsflags.domain.update.GetAppUpdatePolicy

/**
 * Backs both update surfaces: the blocking sheet and the top-level notice. Dismissing the sheet
 * snoozes the policy for the next launch but leaves the notice in place for the current session.
 */
class AppUpdateViewModel(
    private val getAppUpdatePolicy: GetAppUpdatePolicy,
    private val dismissSoftAppUpdate: DismissSoftAppUpdate,
    forcedNoticePolicy: AppUpdatePolicy? = null,
) : ViewModel() {
    private val policy = MutableStateFlow<AppUpdatePolicy?>(null)
    private val sheetDismissed = MutableStateFlow(false)

    val sheetPolicy: StateFlow<AppUpdatePolicy?> = combine(policy, sheetDismissed) { current, dismissed ->
        current?.takeUnless { dismissed }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val noticePolicy: StateFlow<AppUpdatePolicy?> = policy
        .map { it ?: forcedNoticePolicy }
        .stateIn(viewModelScope, SharingStarted.Eagerly, forcedNoticePolicy)

    init {
        viewModelScope.launch {
            policy.value = runCatching { getAppUpdatePolicy() }.getOrNull()
        }
    }

    /** Ignored for FORCE — the sheet stays until the user updates. */
    fun dismiss() {
        val current = policy.value ?: return
        if (current.type == AppUpdateType.FORCE) return
        sheetDismissed.value = true
        viewModelScope.launch {
            dismissSoftAppUpdate(current.policyId, current.softCooldownHours)
        }
    }
}
