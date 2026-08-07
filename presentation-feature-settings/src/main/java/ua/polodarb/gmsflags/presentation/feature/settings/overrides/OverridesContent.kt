package ua.polodarb.gmsflags.presentation.feature.settings.overrides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.feature.settings.ui.SettingsRow
import ua.polodarb.gmsflags.presentation.feature.settings.ui.SettingsSection
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.presentation.feature.settings.ui.SettingsSectionCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OverridesContent(
    state: OverridesState,
    onBack: () -> Unit,
    onEvent: (OverridesEvent) -> Unit,
) {
    TrackScreenView(AnalyticsScreen.Overrides)

    val layout = LocalGmsAdaptiveLayout.current
    val uriHandler = LocalUriHandler.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = { GmsBackHeader(stringResource(R.string.settings_overrides_title), onBack) },
    ) { padding ->
        GmsContentContainer(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = layout.contentPadding,
                    vertical = GmsSpacing.ExtraLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
            ) {
                item {
                    SettingsSection(stringResource(R.string.settings_runtime_section)) {
                        SettingsRow(
                            title = stringResource(
                                if (state.control.paused) R.string.settings_resume_overrides
                                else R.string.settings_pause_overrides
                            ),
                            description = stringResource(
                                if (state.control.paused) R.string.settings_resume_overrides_description
                                else R.string.settings_pause_overrides_description
                            ),
                            icon = if (state.control.paused) Icons.Outlined.PlayCircle else Icons.Outlined.PauseCircle,
                            onClick = { onEvent(OverridesEvent.PauseClicked) },
                            trailing = if (state.busy) {
                                { CircularProgressIndicator(modifier = Modifier.padding(8.dp)) }
                            } else null,
                        )
                    }
                }
                item {
                    SettingsSection(
                        title = stringResource(R.string.settings_data_section),
                        trailing = {
                            SettingsSectionCount(
                                stringResource(
                                    R.string.settings_saved_overrides_count,
                                    state.control.overrideCount,
                                ),
                            )
                        },
                    ) {
                        SettingsRow(
                            title = stringResource(R.string.settings_import_flags),
                            description = stringResource(R.string.settings_import_flags_description),
                            icon = Icons.Outlined.FolderOpen,
                            onClick = { onEvent(OverridesEvent.ImportClicked) },
                        )
                        SettingsRow(
                            title = stringResource(R.string.settings_remove_all),
                            description = stringResource(R.string.settings_remove_all_description),
                            icon = Icons.Outlined.DeleteSweep,
                            onClick = { onEvent(OverridesEvent.DeleteAllClicked) },
                            destructive = true,
                        )
                    }
                }
                item {
                    SettingsSection(stringResource(R.string.settings_diagnostics_section)) {
                        SettingsRow(
                            title = stringResource(R.string.settings_privacy_policy),
                            description = PRIVACY_POLICY_URL.removePrefix("https://"),
                            icon = Icons.Outlined.PrivacyTip,
                            onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
                        )
                        SettingsRow(
                            title = stringResource(R.string.settings_terms),
                            description = TERMS_URL.removePrefix("https://"),
                            icon = Icons.Outlined.Description,
                            onClick = { uriHandler.openUri(TERMS_URL) },
                        )
                        SettingsRow(
                            title = stringResource(R.string.settings_analytics_title),
                            description = stringResource(R.string.settings_analytics_description),
                            icon = Icons.Outlined.Insights,
                            onClick = {
                                onEvent(
                                    OverridesEvent.AnalyticsConsentToggled(!state.analyticsEnabled),
                                )
                            },
                            trailing = {
                                Switch(
                                    checked = state.analyticsEnabled,
                                    onCheckedChange = {
                                        onEvent(OverridesEvent.AnalyticsConsentToggled(it))
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    state.confirmation?.let { confirmation ->
        ConfirmationSheet(
            confirmation = confirmation,
            onDismiss = { onEvent(OverridesEvent.ConfirmationDismissed) },
            onConfirm = { onEvent(OverridesEvent.ConfirmationAccepted) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationSheet(
    confirmation: OverrideConfirmation,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val title = when (confirmation) {
        OverrideConfirmation.Pause -> R.string.settings_pause_sheet_title
        OverrideConfirmation.Resume -> R.string.settings_resume_sheet_title
        OverrideConfirmation.DeleteAll -> R.string.settings_remove_sheet_title
    }
    val description = when (confirmation) {
        OverrideConfirmation.Pause -> R.string.settings_pause_sheet_description
        OverrideConfirmation.Resume -> R.string.settings_resume_sheet_description
        OverrideConfirmation.DeleteAll -> R.string.settings_remove_sheet_description
    }
    val action = when (confirmation) {
        OverrideConfirmation.Pause -> R.string.settings_action_pause
        OverrideConfirmation.Resume -> R.string.settings_action_resume
        OverrideConfirmation.DeleteAll -> R.string.settings_action_remove
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GmsSpacing.ExtraLarge)
                .padding(bottom = GmsSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Text(stringResource(title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = GmsSpacing.Large),
            ) { Text(stringResource(action)) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_action_cancel))
            }
        }
    }
}

private const val PRIVACY_POLICY_URL = "https://api.polodarb.com/gmsflags/privacy"
private const val TERMS_URL = "https://api.polodarb.com/gmsflags/terms"
