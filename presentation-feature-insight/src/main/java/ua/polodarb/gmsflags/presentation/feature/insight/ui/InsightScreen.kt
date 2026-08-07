package ua.polodarb.gmsflags.presentation.feature.insight.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.compose.koinInject
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker

@Composable
fun InsightScreen(onSettingsSelected: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val analytics = koinInject<AnalyticsTracker>()
    var installed by remember { mutableStateOf(context.isGmsInsightInstalled()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        installed = context.isGmsInsightInstalled()
    }

    InsightContent(
        installed = installed,
        onSettingsClick = onSettingsSelected,
        onActionClick = {
            if (installed) {
                analytics.track(AnalyticsEvent.insightOpenClick())
                context.launchGmsInsight()
            } else {
                analytics.track(AnalyticsEvent.insightPlayStoreClick())
                uriHandler.openUri(GMS_INSIGHT_PLAY_STORE_URL)
            }
        },
    )
}

private const val GMS_INSIGHT_PACKAGE_NAME = "ua.polodarb.gmsinsight"
private const val GMS_INSIGHT_PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=$GMS_INSIGHT_PACKAGE_NAME"

private fun Context.isGmsInsightInstalled(): Boolean =
    packageManager.getLaunchIntentForPackage(GMS_INSIGHT_PACKAGE_NAME) != null

private fun Context.launchGmsInsight() {
    val intent = packageManager.getLaunchIntentForPackage(GMS_INSIGHT_PACKAGE_NAME) ?: return
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
