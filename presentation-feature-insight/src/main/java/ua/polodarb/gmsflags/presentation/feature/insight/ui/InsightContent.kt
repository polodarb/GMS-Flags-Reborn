package ua.polodarb.gmsflags.presentation.feature.insight.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsTopLevelScreen
import ua.polodarb.gmsflags.presentation.feature.insight.ui.components.InsightCapabilities
import ua.polodarb.gmsflags.presentation.feature.insight.ui.components.InsightHero

@Composable
fun InsightContent(
    installed: Boolean,
    onSettingsClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    TrackScreenView(AnalyticsScreen.Insight)

    GmsTopLevelScreen(
        onSettingsClick = onSettingsClick,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            item(key = "hero") { InsightHero(installed = installed, onActionClick = onActionClick) }
            item(key = "capabilities") { InsightCapabilities() }
        }
    }
}
