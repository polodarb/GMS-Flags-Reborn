package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import ua.polodarb.gmsflags.domain.server.content.DangerLevel
import ua.polodarb.gmsflags.domain.server.content.RemoteFlagValueType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyValueBadge
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendedFlagUiModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun RecommendedFlagCard(
    flag: RecommendedFlagUiModel,
    defaultPackageName: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    onLongClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val effectivePackageName = flag.packageName ?: defaultPackageName
    val hasPackageInfo = effectivePackageName != null
    var expanded by remember(flag.name) { mutableStateOf(false) }
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val rotationSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = rotationSpatialSpec,
        label = "flagPackageChevronRotation",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (hasPackageInfo) expanded = !expanded },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (flag.dangerLevel == DangerLevel.Caution ||
                    flag.dangerLevel == DangerLevel.Dangerous
                ) {
                    Icon(
                        Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    flag.title?.let {
                        Text(it, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = flag.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (hasPackageInfo) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.rotate(chevronRotation),
                    )
                }
            }
            if (hasPackageInfo) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(animationSpec = spatialSpec) +
                        fadeIn(animationSpec = effectsSpec),
                    exit = shrinkVertically(animationSpec = spatialSpec) +
                        fadeOut(animationSpec = effectsSpec),
                ) {
                    Text(
                        text = stringResource(
                            R.string.suggestions_details_flag_package,
                            effectivePackageName.orEmpty(),
                        ),
                        modifier = Modifier.padding(top = GmsSpacing.Small),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            flag.description?.let {
                Text(
                    it,
                    modifier = Modifier.padding(top = GmsSpacing.Small),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = GmsSpacing.Small),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(flag.type.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (flag.value.isEmpty()) {
                        GmsEmptyValueBadge()
                    } else {
                        Text(
                            text = flag.value,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (!flag.supported) {
                Text(
                    text = stringResource(R.string.suggestions_details_flag_type_unsupported),
                    modifier = Modifier.padding(top = GmsSpacing.Small),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (flag.badges.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = GmsSpacing.Small),
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                ) {
                    flag.badges.forEach { badge ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                badge.label,
                                modifier = Modifier.padding(
                                    horizontal = GmsSpacing.Small,
                                    vertical = GmsSpacing.ExtraSmall,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun RemoteFlagValueType.labelRes(): Int = when (this) {
    RemoteFlagValueType.Boolean -> R.string.suggestions_flag_type_bool
    RemoteFlagValueType.Integer -> R.string.suggestions_flag_type_int
    RemoteFlagValueType.Long -> R.string.suggestions_flag_type_long
    RemoteFlagValueType.Float -> R.string.suggestions_flag_type_float
    RemoteFlagValueType.Double -> R.string.suggestions_flag_type_double
    RemoteFlagValueType.String -> R.string.suggestions_flag_type_string
    RemoteFlagValueType.Bytes -> R.string.suggestions_flag_type_bytes
    RemoteFlagValueType.Unknown -> R.string.suggestions_flag_type_unknown
}
