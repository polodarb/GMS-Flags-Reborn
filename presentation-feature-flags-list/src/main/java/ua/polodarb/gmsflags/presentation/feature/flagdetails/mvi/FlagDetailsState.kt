package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.server.content.ServerFlagCatalogEntry
import ua.polodarb.gmsflags.domain.server.content.ServerInfoBlock
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary

enum class FlagFilter { All, Changed, Disabled, Enabled }

sealed interface FlagDetailsDialog {
    data class Editor(
        val originalName: String?,
        val name: String,
        val type: FlagType,
        val value: String,
    ) : FlagDetailsDialog

    data object DeleteAllOverrides : FlagDetailsDialog
    data class Export(val fileName: String) : FlagDetailsDialog
    data class ShareToCommunity(
        val packageName: String,
        val flags: List<ua.polodarb.gmsflags.domain.community.CommunityFlagItem>,
    ) : FlagDetailsDialog
    data class Report(val description: String) : FlagDetailsDialog
}


data class SelectedFlag(val type: FlagType, val name: String)

data class InlineFlagEditor(
    val name: String,
    val type: FlagType,
    val value: String,
)

sealed interface AppRemoteContentState {
    data object Loading : AppRemoteContentState
    data object Unavailable : AppRemoteContentState
    data class Error(val error: UiError) : AppRemoteContentState
    data class Ready(
        val infoBlocks: List<ServerInfoBlock>,
        val recommendations: List<ServerRecommendationSummary>,
        val flagAnnotations: Map<String, ServerFlagCatalogEntry>,
    ) : AppRemoteContentState
}

data class FlagDetailsState(
    val androidPackageName: String,
    val applicationName: String = androidPackageName,
    val phenotypePackageName: String,
    val primaryPhenotypePackageName: String = phenotypePackageName,
    val availablePhenotypePackageNames: List<String> = listOf(phenotypePackageName),
    val loading: Boolean = true,
    val operationInProgress: Boolean = false,
    val bulkOperationInProgress: Boolean = false,
    val flags: List<PhenotypeFlag> = emptyList(),
    val selectedType: FlagType = FlagType.Boolean,
    val filter: FlagFilter = FlagFilter.All,
    val query: String = "",
    val effectiveQuery: String = "",
    val searchVisible: Boolean = false,
    val filtersVisible: Boolean = false,
    val selectedFlags: Set<SelectedFlag> = emptySet(),
    val inlineEditor: InlineFlagEditor? = null,
    val dialog: FlagDetailsDialog? = null,
    val error: UiError? = null,
    val xposedScopeStatus: XposedScopeStatus = XposedScopeStatus.Unknown,
    val scopeHelpVisible: Boolean = false,
    val pairipIncompatible: Boolean = false,
    val pairipHelpVisible: Boolean = false,
    val booleanControlHelpVisible: Boolean = false,
    val remoteContent: AppRemoteContentState = AppRemoteContentState.Loading,
) : ViewState {
    val selectionMode: Boolean get() = selectedFlags.isNotEmpty()
}

internal fun FlagDetailsState.visibleFlags(): List<PhenotypeFlag> = flags.asSequence()
    .filter { it.type == selectedType }
    .filter { flag ->
        when (filter) {
            FlagFilter.All -> true
            FlagFilter.Changed -> flag.overridden
            FlagFilter.Enabled -> flag.type != FlagType.Boolean || flag.value.isEnabledBoolean()
            FlagFilter.Disabled -> flag.type != FlagType.Boolean || !flag.value.isEnabledBoolean()
        }
    }
    .filter { it.name.contains(effectiveQuery, ignoreCase = true) }
    .toList()

internal fun String.isEnabledBoolean(): Boolean = this == "1" || equals("true", ignoreCase = true)
