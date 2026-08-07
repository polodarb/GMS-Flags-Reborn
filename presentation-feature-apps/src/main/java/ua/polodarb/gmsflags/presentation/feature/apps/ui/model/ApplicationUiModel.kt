package ua.polodarb.gmsflags.presentation.feature.apps.ui.model

import androidx.compose.runtime.Immutable
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus

@Immutable
data class ApplicationUiModel(
    val androidPackageName: String,
    val name: String,
    val mainFlagPackageName: String,
    val flagPackages: List<FlagPackageUiModel>,
    val scopeStatus: XposedScopeStatusUi,
    val unsupported: Boolean = false,
    val unsupportedFromVersion: Long? = null,
    val pairipIncompatible: Boolean = false,
)

@Immutable
data class FlagPackageUiModel(
    val packageName: String,
    val category: FlagPackageCategoryUi,
)

enum class FlagPackageCategoryUi { Primary, Secondary }
enum class XposedScopeStatusUi { Included, Excluded, Unknown }

internal fun SupportedApplication.toUiModel(): ApplicationUiModel? {
    val mainPackage = mainFlagPackage ?: return null
    return ApplicationUiModel(
        androidPackageName = androidPackageName,
        name = name,
        mainFlagPackageName = mainPackage.packageName,
        flagPackages = flagPackages.map { flagPackage ->
            FlagPackageUiModel(
                packageName = flagPackage.packageName,
                category = when (flagPackage.category) {
                    FlagPackageCategory.Primary -> FlagPackageCategoryUi.Primary
                    FlagPackageCategory.Secondary -> FlagPackageCategoryUi.Secondary
                },
            )
        },
        scopeStatus = when (xposedScopeStatus) {
            XposedScopeStatus.Included -> XposedScopeStatusUi.Included
            XposedScopeStatus.Excluded -> XposedScopeStatusUi.Excluded
            XposedScopeStatus.Unknown -> XposedScopeStatusUi.Unknown
        },
        unsupported = isUnsupported,
        unsupportedFromVersion = disabledFromVersion?.takeIf { isUnsupported },
        pairipIncompatible = pairipIncompatible,
    )
}
