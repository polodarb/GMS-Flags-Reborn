package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ExternalImportContent(
    state: ExternalImportState,
    onEvent: (ExternalImportEvent) -> Unit,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(R.string.import_flags_title),
                onBack = { onEvent(ExternalImportEvent.BackClicked) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            GmsContentContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = adaptiveLayout.contentMaxWidth),
            ) {
                when {
                    state.loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                    state.errorMessageRes != null -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(adaptiveLayout.contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(stringResource(state.errorMessageRes))
                        Button(onClick = { onEvent(ExternalImportEvent.RetryClicked) }) {
                            Text(stringResource(R.string.import_flags_retry))
                        }
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(adaptiveLayout.contentPadding),
                        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.import_flags_choose_application),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        items(
                            items = state.targets,
                            key = { it.androidPackageName },
                        ) { target ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.large)
                                    .clickable {
                                        onEvent(
                                            ExternalImportEvent.TargetSelected(
                                                target.androidPackageName
                                            )
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(GmsSpacing.ExtraLarge),
                                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                                ) {
                                    Text(
                                        text = target.applicationName,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        text = target.androidPackageName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    state.unsupportedPackageName?.let { packageName ->
        AlertDialog(
            onDismissRequest = { onEvent(ExternalImportEvent.BackClicked) },
            title = {
                Text(stringResource(R.string.import_flags_unsupported_package_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.import_flags_unsupported_external_package_message,
                        packageName,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(ExternalImportEvent.BackClicked) }) {
                    Text(stringResource(R.string.action_continue))
                }
            },
        )
    }
}
