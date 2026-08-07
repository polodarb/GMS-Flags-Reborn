package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class RecommendationAppGroupUiModel(
    val packageName: String,
    val displayName: String,
    val iconUrl: String?,
    val recommendations: List<RecommendationUiModel>,
)

internal fun List<RecommendationUiModel>.groupedByApplication(): List<RecommendationAppGroupUiModel> =
    mapNotNull { recommendation ->
        val packageName = recommendation.applicationPackageName ?: return@mapNotNull null
        packageName to recommendation
    }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        .map { (packageName, recommendations) ->
            val application = recommendations.first()
            RecommendationAppGroupUiModel(
                packageName = packageName,
                displayName = application.applicationName ?: packageName,
                iconUrl = application.applicationIconUrl ?: application.logoUrl,
                recommendations = recommendations,
            )
        }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, RecommendationAppGroupUiModel::displayName))
