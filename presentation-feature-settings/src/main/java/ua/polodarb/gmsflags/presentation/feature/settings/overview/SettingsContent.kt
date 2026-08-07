package ua.polodarb.gmsflags.presentation.feature.settings.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.application.ApplicationIconProvider
import ua.polodarb.gmsflags.presentation.core.ui.application.GmsApplicationIcon
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.DevelopersSection
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.SettingsBrandLockup
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.FaqCard
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.HookStatusCard
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.OverridesStorageCard
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.SettingsSocialBar
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.ServerConnectionCard
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.AnimatedSettingsLogo
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.SupportHeaderPill
import ua.polodarb.gmsflags.presentation.feature.settings.overview.components.FeedbackHeaderPill
import ua.polodarb.gmsflags.presentation.feature.settings.feedback.SendFeedbackSheet
import ua.polodarb.gmsflags.presentation.feature.settings.support.SupportSheet

@Composable
internal fun SettingsContent(
    state: SettingsState,
    onBack: () -> Unit,
    onEvent: (SettingsEvent) -> Unit,
) {
    TrackScreenView(AnalyticsScreen.Settings)

    val context = LocalContext.current
    val resources = LocalResources.current
    val uriHandler = LocalUriHandler.current
    val iconProvider: ApplicationIconProvider = koinInject()
    val layout = LocalGmsAdaptiveLayout.current
    var supportSheetVisible by remember { mutableStateOf(false) }
    var feedbackSheetVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(R.string.settings_title),
                onBack = onBack,
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FeedbackHeaderPill(
                            onClick = { feedbackSheetVisible = true },
                        )
                        SupportHeaderPill(
                            onClick = {
                                onEvent(SettingsEvent.SupportClicked)
                                supportSheetVisible = true
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            SettingsSocialBar(
                onTelegramClick = { uriHandler.openUri(TELEGRAM_URL) },
                onXClick = { uriHandler.openUri(X_URL) },
            )
        },
    ) { padding ->
        GmsContentContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = layout.contentPadding,
                    top = GmsSpacing.Small,
                    end = layout.contentPadding,
                    bottom = padding.calculateBottomPadding() + GmsSpacing.Small,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            ) {
                item {
                    AnimatedSettingsLogo {
                        GmsApplicationIcon(
                            packageName = context.packageName,
                            applicationName = stringResource(R.string.settings_app_name),
                            iconProvider = iconProvider,
                            modifier = Modifier
                                .size(148.dp)
                                .graphicsLayer {
                                    scaleX = 2f
                                    scaleY = 2f
                                },
                            foregroundOnly = true,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                item {
                    SettingsBrandLockup(
                        modifier = Modifier.padding(bottom = GmsSpacing.ExtraLarge),
                    )
                }
                item {
                    ServerConnectionCard(
                        state = state.serverConnection,
                        resources = resources,
                    )
                }
                item {
                    HookStatusCard(
                        state = state,
                        resources = resources,
                        onClick = { onEvent(SettingsEvent.HookStatusClicked) },
                    )
                }
                item {
                    OverridesStorageCard(
                        overrideControl = state.overrideControl,
                        error = state.overrideError,
                        resources = resources,
                        onClick = { onEvent(SettingsEvent.OverridesClicked) },
                    )
                }
                item {
                    FaqCard(onClick = { onEvent(SettingsEvent.FaqClicked) })
                }
                item {
                    DevelopersSection(
                        versionName = context.packageManager
                            .getPackageInfo(context.packageName, 0)
                            .versionName
                            .orEmpty(),
                        onDanyilClick = { uriHandler.openUri(DANYIL_URL) },
                        onTransaeroClick = { uriHandler.openUri(TRANSAERO_URL) },
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (supportSheetVisible) {
        SupportSheet(onDismiss = { supportSheetVisible = false })
    }
    if (feedbackSheetVisible) {
        SendFeedbackSheet(onDismiss = { feedbackSheetVisible = false })
    }
}

private const val DANYIL_URL = "https://github.com/polodarb"
private const val TRANSAERO_URL = "https://github.com/transaero21"
private const val TELEGRAM_URL = "https://t.me/polodarb_dev"
private const val X_URL = "https://x.com/polodarb"
