package ua.polodarb.gmsflags.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

/** Emits a screen_view for [screen] once per entry into the composition. */
@Composable
fun TrackScreenView(screen: AnalyticsScreen) {
    val tracker: AnalyticsTracker = koinInject()
    LaunchedEffect(screen) {
        tracker.track(AnalyticsEvent.screenView(screen))
    }
}
