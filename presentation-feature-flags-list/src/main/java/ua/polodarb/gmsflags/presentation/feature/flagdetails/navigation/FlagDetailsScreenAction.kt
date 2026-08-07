package ua.polodarb.gmsflags.presentation.feature.flagdetails.navigation

sealed interface FlagDetailsScreenAction {
    data object Back : FlagDetailsScreenAction
    data class AddMultiple(
        val androidPackageName: String,
        val phenotypePackageName: String,
    ) : FlagDetailsScreenAction

    data class ImportFlags(
        val androidPackageName: String,
        val applicationName: String,
        val currentPhenotypePackageName: String,
        val supportedPhenotypePackageNames: List<String>,
        val documentUri: String,
    ) : FlagDetailsScreenAction

    data class OpenRecommendation(val id: Long) : FlagDetailsScreenAction
}
