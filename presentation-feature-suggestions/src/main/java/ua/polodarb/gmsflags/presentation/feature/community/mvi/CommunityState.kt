package ua.polodarb.gmsflags.presentation.feature.community.mvi

import androidx.compose.runtime.Immutable
import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.domain.community.CommunityPackage


@Immutable
data class CommunityState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val packages: List<CommunityPackage> = emptyList(),
    val selectedPackage: CommunityPackage? = null,
    val isDisclaimerDismissed: Boolean = false,
    val showDisclaimerDialog: Boolean = false,
    val showReportDialog: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val submitPackageName: String = "",
    val submitTitle: String = "",
    val submitDescription: String = "",
    val submitFlags: List<CommunityFlagItem> = emptyList(),
) {
    val filteredPackages: List<CommunityPackage>
        get() {
            if (searchQuery.isBlank()) return packages
            val q = searchQuery.trim().lowercase()
            return packages.filter { pkg ->
                pkg.title.lowercase().contains(q) ||
                        (pkg.description?.lowercase()?.contains(q) == true) ||
                        pkg.packageName.lowercase().contains(q) ||
                        pkg.flags.any { it.flagName.lowercase().contains(q) }
            }
        }
}
