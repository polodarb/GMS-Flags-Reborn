package ua.polodarb.gmsflags.presentation.feature.community.mvi

import ua.polodarb.gmsflags.domain.community.CommunityPackage
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

data class CommunityState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val searchVisible: Boolean = false,
    val packages: List<CommunityPackage> = emptyList(),
    val selectedPackage: CommunityPackage? = null,
    val showSubmitDialog: Boolean = false,
    val showDisclaimer: Boolean = false,
) : ViewState
