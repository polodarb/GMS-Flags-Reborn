package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import ua.polodarb.gmsflags.domain.flags.FlagType

internal object FlagDetailsReducer {
    fun reduce(
        state: FlagDetailsState,
        event: FlagDetailsEvent,
    ): FlagDetailsReduction? = when (event) {
        FlagDetailsEvent.BackClicked -> when {
            state.selectionMode -> state.copy(selectedFlags = emptySet()).asReduction()
            state.inlineEditor != null -> state.copy(inlineEditor = null).asReduction()
            else -> state.asReduction(FlagDetailsEffect.NavigateBack)
        }
        FlagDetailsEvent.PackageNameLongClicked -> state.asReduction(
            FlagDetailsEffect.CopyPackageName(state.phenotypePackageName)
        )
        FlagDetailsEvent.CopySelectedFlagNamesClicked -> if (state.selectedFlags.isEmpty()) {
            state.asReduction()
        } else {
            state.asReduction(
                FlagDetailsEffect.CopyFlagNames(state.selectedFlags.map { it.name })
            )
        }
        is FlagDetailsEvent.TypeSelected -> if (state.selectionMode) {
            state.asReduction()
        } else {
            state.copy(
                selectedType = event.type,
                filter = state.filter.validFor(event.type),
                inlineEditor = null,
            ).asReduction()
        }
        FlagDetailsEvent.SearchToggled -> state.copy(
            searchVisible = !state.searchVisible,
            filtersVisible = false,
            query = if (state.searchVisible) "" else state.query,
            effectiveQuery = if (state.searchVisible) "" else state.effectiveQuery,
            inlineEditor = null,
        ).asReduction()
        is FlagDetailsEvent.QueryChanged -> state.copy(
            query = event.query,
            inlineEditor = null,
        ).asReduction()
        is FlagDetailsEvent.QueryDebounced -> if (state.query == event.query) {
            state.copy(effectiveQuery = event.query).asReduction()
        } else {
            state.asReduction()
        }
        FlagDetailsEvent.FiltersToggled -> state.copy(
            filtersVisible = !state.filtersVisible,
            searchVisible = false,
            query = "",
            effectiveQuery = "",
            inlineEditor = null,
        ).asReduction()
        is FlagDetailsEvent.FilterSelected -> state.copy(
            filter = event.filter,
            inlineEditor = null,
        ).asReduction()
        is FlagDetailsEvent.FlagLongClicked -> state.copy(inlineEditor = null)
            .toggleSelection(event.name)
            .asReduction()
        is FlagDetailsEvent.FlagClicked -> state.onFlagClicked(event.name).asReduction()
        is FlagDetailsEvent.DragSelectionChanged -> state.copy(
            inlineEditor = null,
            selectedFlags = event.selectedFlags,
        ).asReduction()
        is FlagDetailsEvent.InlineEditorValueChanged -> state.updateInlineEditor(event.value)
            .asReduction()
        FlagDetailsEvent.InlineEditorDismissed -> state.copy(inlineEditor = null).asReduction()
        FlagDetailsEvent.ExitSelection -> state.copy(selectedFlags = emptySet()).asReduction()
        FlagDetailsEvent.SelectAll -> state.selectAllVisible().asReduction()
        FlagDetailsEvent.AddFlagClicked -> state.openAddFlagDialog().asReduction()
        FlagDetailsEvent.AddMultipleClicked -> state.asReduction(
            FlagDetailsEffect.OpenAddMultiple(
                state.androidPackageName,
                state.phenotypePackageName,
            )
        )
        FlagDetailsEvent.ImportFlagsClicked -> state.asReduction(
            FlagDetailsEffect.OpenImportFilePicker
        )
        FlagDetailsEvent.DeleteAllOverridesClicked -> state.copy(
            dialog = FlagDetailsDialog.DeleteAllOverrides
        ).asReduction()
        FlagDetailsEvent.LaunchApplicationClicked -> state.asReduction(
            FlagDetailsEffect.LaunchApplication(state.androidPackageName)
        )
        FlagDetailsEvent.OpenAppSettingsClicked -> state.asReduction(
            FlagDetailsEffect.OpenAppSettings(state.androidPackageName)
        )
        is FlagDetailsEvent.EditorNameChanged -> state.updateEditor { editor ->
            if (editor.originalName == null) editor.copy(name = event.value) else editor
        }.asReduction()
        is FlagDetailsEvent.EditorTypeChanged -> state.updateEditor {
            it.withType(event.value)
        }.asReduction()
        is FlagDetailsEvent.EditorValueChanged -> state.updateEditor {
            it.copy(value = event.value)
        }.asReduction()
        FlagDetailsEvent.ExportClicked -> if (state.selectedFlags.isEmpty()) {
            state.asReduction()
        } else {
            state.copy(
                dialog = FlagDetailsDialog.Export(fileName = state.phenotypePackageName)
            ).asReduction()
        }
        is FlagDetailsEvent.ExportFileNameChanged -> state.updateExportDialog(event.value)
            .asReduction()
        FlagDetailsEvent.ShareToCommunityClicked -> {
            val selected = state.selectedFlagValues()
            val initialFlags = if (selected.isNotEmpty()) {
                selected.map { flag ->
                    ua.polodarb.gmsflags.domain.community.CommunityFlagItem(
                        flagName = flag.name,
                        valueType = flag.type.name.lowercase(),
                        value = flag.value,
                    )
                }
            } else {
                emptyList()
            }
            state.copy(
                dialog = FlagDetailsDialog.ShareToCommunity(
                    packageName = state.phenotypePackageName,
                    flags = initialFlags,
                )
            ).asReduction()
        }
        is FlagDetailsEvent.AddCommunityFlag -> {
            val currentDialog = state.dialog as? FlagDetailsDialog.ShareToCommunity
            if (currentDialog != null) {
                val newFlag = ua.polodarb.gmsflags.domain.community.CommunityFlagItem(
                    flagName = event.flagName,
                    valueType = event.valueType,
                    value = event.value,
                )
                state.copy(
                    dialog = currentDialog.copy(flags = currentDialog.flags + newFlag)
                ).asReduction()
            } else {
                state.asReduction()
            }
        }
        is FlagDetailsEvent.RemoveCommunityFlag -> {
            val currentDialog = state.dialog as? FlagDetailsDialog.ShareToCommunity
            if (currentDialog != null && event.index in currentDialog.flags.indices) {
                val updatedFlags = currentDialog.flags.toMutableList().apply { removeAt(event.index) }
                state.copy(
                    dialog = currentDialog.copy(flags = updatedFlags)
                ).asReduction()
            } else {
                state.asReduction()
            }
        }

        FlagDetailsEvent.ReportClicked -> state.copy(
            dialog = FlagDetailsDialog.Report(description = "")
        ).asReduction()
        is FlagDetailsEvent.ReportDescriptionChanged -> state.updateReportDialog(event.value)
            .asReduction()
        is FlagDetailsEvent.SubmitCommunityPackage -> state.copy(dialog = null).asReduction()
        FlagDetailsEvent.DialogDismissed -> state.copy(dialog = null).asReduction()

        FlagDetailsEvent.Retry,

        is FlagDetailsEvent.PackageSelected,
        is FlagDetailsEvent.BooleanChanged,
        is FlagDetailsEvent.BooleanOverrideCleared,
        FlagDetailsEvent.InlineEditorSaved,
        FlagDetailsEvent.InlineEditorReset,
        is FlagDetailsEvent.SetSelectedBooleans,
        FlagDetailsEvent.ResetSelectedToDefault,
        FlagDetailsEvent.DeleteAllOverridesConfirmed,
        FlagDetailsEvent.EditorSaved,
        FlagDetailsEvent.EditorReset,
        FlagDetailsEvent.ExportConfirmed,
        FlagDetailsEvent.ReportConfirmed,
        FlagDetailsEvent.ScopeRefresh,
        FlagDetailsEvent.ScopeHelpClicked,
        FlagDetailsEvent.ScopeHelpDismissed,
        FlagDetailsEvent.PairipHelpClicked,
        FlagDetailsEvent.PairipHelpDismissed,
        FlagDetailsEvent.BooleanControlHelpClicked,
        FlagDetailsEvent.BooleanControlHelpDismissed,
        FlagDetailsEvent.RemoteContentRetry,
        is FlagDetailsEvent.RecommendationClicked -> null
    }
}

internal data class FlagDetailsReduction(
    val state: FlagDetailsState,
    val effect: FlagDetailsEffect? = null,
)

private fun FlagDetailsState.asReduction(
    effect: FlagDetailsEffect? = null,
) = FlagDetailsReduction(this, effect)

private fun FlagDetailsState.onFlagClicked(name: String): FlagDetailsState {
    if (selectionMode) return toggleSelection(name)
    val flag = flags.firstOrNull { it.type == selectedType && it.name == name } ?: return this
    if (flag.type == FlagType.Boolean) return this
    return if (inlineEditor?.name == flag.name && inlineEditor.type == flag.type) {
        copy(inlineEditor = null)
    } else {
        copy(
            inlineEditor = InlineFlagEditor(
                name = flag.name,
                type = flag.type,
                value = flag.value,
            ),
        )
    }
}

private fun FlagDetailsState.updateInlineEditor(value: String): FlagDetailsState {
    val editor = inlineEditor ?: return this
    return copy(inlineEditor = editor.copy(value = value))
}

private fun FlagDetailsState.openAddFlagDialog(): FlagDetailsState = copy(
    dialog = FlagDetailsDialog.Editor(
        originalName = null,
        name = "",
        type = selectedType,
        value = if (selectedType == FlagType.Boolean) "1" else "",
    )
)

private fun FlagDetailsState.updateEditor(
    transform: (FlagDetailsDialog.Editor) -> FlagDetailsDialog.Editor,
): FlagDetailsState {
    val editor = dialog as? FlagDetailsDialog.Editor ?: return this
    return copy(dialog = transform(editor))
}

private fun FlagDetailsDialog.Editor.withType(type: FlagType): FlagDetailsDialog.Editor {
    val normalizedValue = when (type) {
        FlagType.Boolean -> if (value.isEnabledBoolean()) "1" else "0"
        FlagType.Integer -> value.trim().takeIf { it.toLongOrNull() != null }.orEmpty()
        FlagType.Float -> value
            .trim()
            .replace(',', '.')
            .takeIf { it.toDoubleOrNull() != null }
            .orEmpty()
        FlagType.String -> value
    }
    return copy(type = type, value = normalizedValue)
}

private fun FlagDetailsState.updateExportDialog(value: String): FlagDetailsState {
    val export = dialog as? FlagDetailsDialog.Export ?: return this
    return copy(dialog = export.copy(fileName = value))
}

private fun FlagDetailsState.updateReportDialog(value: String): FlagDetailsState {
    val report = dialog as? FlagDetailsDialog.Report ?: return this
    return copy(dialog = report.copy(description = value))
}

private fun FlagFilter.validFor(type: FlagType): FlagFilter = when {
    type == FlagType.Boolean -> this
    this == FlagFilter.All || this == FlagFilter.Changed -> this
    else -> FlagFilter.All
}
