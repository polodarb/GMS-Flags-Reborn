package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationHookUiModel

@Composable
internal fun RecommendedHookCard(
    hook: RecommendationHookUiModel,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    appIntegrityBlocked: Boolean = false,
    onReportProblem: (() -> Unit)? = null,
    onApplyAnyway: (() -> Unit)? = null,
) {
    var trustSheetVisible by remember(hook.hook.recipeId) { mutableStateOf(false) }
    var instructionsSheetVisible by remember(hook.hook.recipeId) { mutableStateOf(false) }
    val failed = !appIntegrityBlocked && hook.trustStatus == HookTrustStatus.VERIFICATION_FAILED
    val blocking = !appIntegrityBlocked && onReportProblem != null && onApplyAnyway != null

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = if (failed || appIntegrityBlocked) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (failed || appIntegrityBlocked) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            HookCardHeader(hook = hook, failed = failed, appIntegrityBlocked = appIntegrityBlocked)

            if (!appIntegrityBlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                    HookTrustChip(
                        status = hook.trustStatus,
                        onFailedCard = failed,
                        onClick = { trustSheetVisible = true },
                    )
                    if (hook.recipeDetails != null) {
                        HookInstructionsChip(onClick = { instructionsSheetVisible = true })
                    }
                }
            }

            if (failed) {
                Text(
                    text = stringResource(
                        if (hook.hook.required) R.string.suggestions_details_patch_failed_required
                        else R.string.suggestions_details_patch_failed_optional
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (blocking) {
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                    Button(
                        onClick = { onReportProblem?.invoke() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(text = stringResource(R.string.suggestions_details_hook_report))
                    }
                    TextButton(
                        onClick = { onApplyAnyway?.invoke() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(text = stringResource(R.string.suggestions_details_hook_apply_anyway))
                    }
                }
            }
        }
    }

    if (trustSheetVisible) {
        RecommendationHookTrustSheet(
            currentStatus = hook.trustStatus,
            onDismiss = { trustSheetVisible = false },
        )
    }
    if (instructionsSheetVisible) {
        hook.recipeDetails?.let { details ->
            RecommendationHookInstructionsSheet(
                details = details,
                onDismiss = { instructionsSheetVisible = false },
            )
        }
    }
}

@Composable
private fun HookCardHeader(
    hook: RecommendationHookUiModel,
    failed: Boolean,
    appIntegrityBlocked: Boolean,
) {
    val purpose = hook.hook.purpose?.trim()?.takeIf(String::isNotEmpty)
    val supporting = when {
        appIntegrityBlocked -> stringResource(R.string.suggestions_details_patch_app_untrusted_body)
        failed -> stringResource(R.string.suggestions_details_patch_failed_body)
        else -> purpose
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        verticalAlignment = if (supporting == null) Alignment.CenterVertically else Alignment.Top,
    ) {
        HookIconBadge(alarming = failed || appIntegrityBlocked)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
        ) {
            Text(
                text = stringResource(
                    when {
                        appIntegrityBlocked -> R.string.suggestions_details_patch_app_untrusted_title
                        failed -> R.string.suggestions_details_patch_failed_title
                        else -> R.string.suggestions_details_patch_title
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(alpha = 0.75f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HookIconBadge(alarming: Boolean) {
    Surface(
        modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
        shape = MaterialTheme.shapes.large,
        color = if (alarming) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = if (alarming) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (alarming) Icons.Rounded.GppBad else Icons.Outlined.Healing,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun HookTrustChip(
    status: HookTrustStatus,
    onFailedCard: Boolean,
    onClick: () -> Unit,
) {
    val appearance = status.appearance()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (onFailedCard) MaterialTheme.colorScheme.error else appearance.containerColor,
        contentColor = if (onFailedCard) MaterialTheme.colorScheme.onError else appearance.contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GmsSpacing.Medium, vertical = GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = appearance.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(appearance.labelRes),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.suggestions_details_patch_learn_more),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun HookInstructionsChip(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GmsSpacing.Medium, vertical = GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.DataObject,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.suggestions_details_hook_instructions_button),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
