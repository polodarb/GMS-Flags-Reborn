package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.sheets

import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.badges.appearance
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecommendationApplicationInfoSheet(
    currentStatus: RecommendationApplicationUiStatus,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val hideSheet: () -> Unit = {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
        Unit
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = GmsSpacing.ExtraLarge),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                    Text(
                        text = stringResource(R.string.suggestions_application_status_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.suggestions_application_status_sheet_description,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                    ApplicationStatuses.forEach { status ->
                        ApplicationStatusRow(
                            status = status,
                            selected = status == currentStatus,
                        )
                    }
                }
            }
            Button(
                onClick = hideSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = GmsSpacing.ExtraLarge,
                        end = GmsSpacing.ExtraLarge,
                        top = GmsSpacing.Large,
                        bottom = GmsSpacing.Small,
                    ),
            ) {
                Text(stringResource(R.string.suggestions_support_sheet_close))
            }
        }
    }
}

@Composable
private fun ApplicationStatusRow(
    status: RecommendationApplicationUiStatus,
    selected: Boolean,
) {
    val appearance = status.appearance()
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            appearance.containerColor
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        contentColor = if (selected) {
            appearance.contentColor
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
                shape = MaterialTheme.shapes.large,
                color = appearance.containerColor,
                contentColor = appearance.contentColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = appearance.icon, contentDescription = null)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(
                    text = stringResource(appearance.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(appearance.descriptionRes),
                    color = if (selected) {
                        appearance.contentColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private val ApplicationStatuses = listOf(
    RecommendationApplicationUiStatus.Applied,
    RecommendationApplicationUiStatus.PartiallyApplied,
    RecommendationApplicationUiStatus.NotApplied,
    RecommendationApplicationUiStatus.Unavailable,
)
