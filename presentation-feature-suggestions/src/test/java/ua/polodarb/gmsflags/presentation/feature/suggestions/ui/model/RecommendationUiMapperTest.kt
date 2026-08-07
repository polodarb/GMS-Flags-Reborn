package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ua.polodarb.gmsflags.domain.server.content.RecommendationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationApplicationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationSupportStatus
import ua.polodarb.gmsflags.domain.server.content.ServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationFeedItem
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary

class RecommendationUiMapperTest {
    @Test
    fun `maps and normalizes recommendation summary`() {
        val result = ServerRecommendationSummary(
            id = 42,
            status = RecommendationStatus.Published,
            supportStatus = RecommendationSupportStatus.Verified,
            logoUrl = " https://example.com/logo.png ",
            title = " New feature ",
            description = " Description ",
            warning = "   ",
        ).toUiModel()

        assertEquals(42, result.id)
        assertEquals("https://example.com/logo.png", result.logoUrl)
        assertEquals("New feature", result.title)
        assertEquals("Description", result.description)
        assertNull(result.warning)
        assertEquals(RecommendationSupportUiModel.Verified, result.supportStatus)
    }

    @Test
    fun `maps application identity for feed navigation`() {
        val result = ServerRecommendationFeedItem(
            summary = ServerRecommendationSummary(
                id = 42,
                status = RecommendationStatus.Published,
                supportStatus = RecommendationSupportStatus.Verified,
                logoUrl = null,
                title = "Feature",
                description = null,
                warning = null,
            ),
            application = ServerApplication(
                id = 7,
                packageName = " com.google.app ",
                displayName = " Google App ",
                iconUrl = " https://example.com/app.png ",
            ),
            previewScreenshotUrl = null,
            flagNames = emptyList(),
            applicationStatus = RecommendationApplicationStatus.NotApplied,
        ).toUiModel()

        assertEquals(7L, result.applicationId)
        assertEquals("Google App", result.applicationName)
        assertEquals("com.google.app", result.applicationPackageName)
        assertEquals("https://example.com/app.png", result.applicationIconUrl)
    }
}
