package ua.polodarb.gmsflags.presentation.core.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ScreenDestination : NavKey

sealed interface RootDestination : ScreenDestination {
    @Serializable
    data object BottomBarFlow : RootDestination

    @Serializable
    data class FlagDetails(
        val androidPackageName: String,
        val applicationName: String,
        val phenotypePackageName: String,
        val availablePhenotypePackageNames: List<String>,
    ) : RootDestination

    @Serializable
    data class AddMultipleFlags(
        val androidPackageName: String,
        val phenotypePackageName: String,
    ) : RootDestination

    @Serializable
    data class ImportFlags(
        val androidPackageName: String,
        val applicationName: String,
        val currentPhenotypePackageName: String,
        val supportedPhenotypePackageNames: List<String>,
        val documentUri: String,
    ) : RootDestination

    @Serializable
    data class ExternalImportFlags(
        val documentUri: String,
    ) : RootDestination

    @Serializable
    data object HookStatus : RootDestination

    @Serializable
    data object Settings : RootDestination

    @Serializable
    data object Faq : RootDestination

    @Serializable
    data object OverridesStorage : RootDestination

    @Serializable
    data class HookStatusDetails(
        val androidPackageName: String,
    ) : RootDestination

    @Serializable
    data class RecommendationDetails(
        val recommendationId: Long,
    ) : RootDestination

}

@Stable
sealed interface BottomBarDestination : ScreenDestination {
    @Serializable
    data object Suggestions : BottomBarDestination

    @Serializable
    data object Apps : BottomBarDestination

    @Serializable
    data object GmsInsight : BottomBarDestination

    @Serializable
    data object Experimental : BottomBarDestination
}
