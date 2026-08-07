package ua.polodarb.gmsflags.domain.apps

data class SupportedApplication(
    val androidPackageName: String,
    val flagPackages: List<FlagPackage>,
    val name: String,
    val versionName: String?,
    val versionCode: Long,
    val lastUpdateTime: Long,
    val preferredFlagPackageName: String? = null,
    val xposedScopeStatus: XposedScopeStatus = XposedScopeStatus.Unknown,
    val disabledFromVersion: Long? = null,
    val pairipIncompatible: Boolean = false,
) {
    val mainFlagPackage: FlagPackage?
        get() = flagPackages.firstOrNull { it.packageName == preferredFlagPackageName }
            ?: flagPackages.firstOrNull { it.packageName == androidPackageName }
            ?: flagPackages.firstOrNull { it.category == FlagPackageCategory.Primary }
            ?: flagPackages.firstOrNull()

    val isUnsupported: Boolean
        get() = disabledFromVersion?.let { versionCode >= it } == true
}

data class FlagPackage(
    val packageName: String,
    val category: FlagPackageCategory,
)

enum class FlagPackageCategory {
    Primary,
    Secondary,
}

enum class XposedScopeStatus {
    Included,
    Excluded,
    Unknown,
}

data class SupportedApplicationsSnapshot(
    val applications: List<SupportedApplication>,
    val moduleStatus: XposedModuleStatus,
)

enum class XposedModuleStatus {
    Enabled,
    Disabled,
    Unknown,
}
