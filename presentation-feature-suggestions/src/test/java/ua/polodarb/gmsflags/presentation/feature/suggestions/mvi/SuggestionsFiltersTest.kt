package ua.polodarb.gmsflags.presentation.feature.suggestions.mvi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationUiModel

class SuggestionsFiltersTest {
    @Test
    fun `query also matches application identity`() {
        val recommendations = listOf(
            recommendation(
                id = 1,
                applicationName = "Google Photos",
                packageName = "com.google.android.apps.photos",
            ),
            recommendation(
                id = 2,
                applicationName = "Gmail",
                packageName = "com.google.android.gm",
            ),
        )

        assertEquals(
            listOf(1L),
            recommendations.filteredByQuery("photos").map(RecommendationUiModel::id),
        )
        assertEquals(
            listOf(2L),
            recommendations.filteredByQuery("android.gm").map(RecommendationUiModel::id),
        )
    }

    @Test
    fun `combines device and support filters`() {
        val recommendations = listOf(
            recommendation(
                id = 1,
                applicationName = "Photos",
                packageName = "photos",
                applicationStatus = RecommendationApplicationUiStatus.NotApplied,
                supportStatus = RecommendationSupportUiModel.Verified,
            ),
            recommendation(
                id = 2,
                applicationName = "Photos",
                packageName = "photos",
                applicationStatus = RecommendationApplicationUiStatus.Applied,
                supportStatus = RecommendationSupportUiModel.Verified,
            ),
            recommendation(
                id = 3,
                applicationName = "Gmail",
                packageName = "gmail",
                applicationStatus = RecommendationApplicationUiStatus.NotApplied,
                supportStatus = RecommendationSupportUiModel.Experimental,
            ),
        )

        val result = recommendations.filteredBy(
            SuggestionsFilters(
                applicationStatus = SuggestionsApplicationStatusFilter.NotEnabled,
                supportStatuses = setOf(RecommendationSupportUiModel.Verified),
            )
        )

        assertEquals(listOf(1L), result.map(RecommendationUiModel::id))
    }

    @Test
    fun `sorts by application without moving unknown apps ahead`() {
        val recommendations = listOf(
            recommendation(id = 1, applicationName = null, packageName = null),
            recommendation(id = 2, applicationName = "YouTube", packageName = "youtube"),
            recommendation(id = 3, applicationName = "Gmail", packageName = "gmail"),
        )

        val result = recommendations.filteredBy(
            SuggestionsFilters(sort = SuggestionsSort.Application)
        )

        assertEquals(listOf(3L, 2L, 1L), result.map(RecommendationUiModel::id))
    }

    @Test
    fun `default filter is inactive`() {
        assertTrue(SuggestionsFilters().isDefault)
        assertEquals(0, SuggestionsFilters().advancedFilterCount)
    }

    private fun recommendation(
        id: Long,
        applicationName: String?,
        packageName: String?,
        applicationStatus: RecommendationApplicationUiStatus =
            RecommendationApplicationUiStatus.Unavailable,
        supportStatus: RecommendationSupportUiModel =
            RecommendationSupportUiModel.Unknown,
    ) = RecommendationUiModel(
        id = id,
        applicationId = id,
        applicationName = applicationName,
        applicationPackageName = packageName,
        applicationIconUrl = null,
        logoUrl = null,
        previewScreenshotUrl = null,
        title = "Feature $id",
        description = null,
        warning = null,
        flagNames = emptyList(),
        supportStatus = supportStatus,
        applicationStatus = applicationStatus,
    )
}
