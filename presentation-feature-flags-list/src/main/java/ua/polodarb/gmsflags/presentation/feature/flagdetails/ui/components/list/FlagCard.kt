package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.domain.server.content.ServerFlagCatalogEntry
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyValueBadge
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.isEnabledBoolean
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.InlineFlagEditor
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.editor.InlineFlagEditorContent

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalAnimationApi::class,
)
@Composable
internal fun FlagCard(
    flag: PhenotypeFlag,
    annotation: ServerFlagCatalogEntry?,
    selected: Boolean,
    selectionMode: Boolean,
    enabled: Boolean,
    onBooleanChanged: (Boolean) -> Unit,
    onBooleanOverrideCleared: () -> Unit,
    inlineEditor: InlineFlagEditor?,
    onInlineValueChanged: (String) -> Unit,
    onInlineSave: () -> Unit,
    onInlineReset: () -> Unit,
    onInlineDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val booleanValue = flag.value.isEnabledBoolean()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = GmsDimensions.FlagCardMinHeight)
            .clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        ),
    ) {
        if (flag.type == FlagType.Boolean) {
            BooleanFlagCardContent(
                flag = flag,
                annotation = annotation,
                value = booleanValue,
                selected = selected,
                selectionMode = selectionMode,
                enabled = enabled,
                onBooleanChanged = onBooleanChanged,
                onOverrideCleared = onBooleanOverrideCleared,
            )
        } else {
            ValueFlagCardContent(
                flag = flag,
                annotation = annotation,
                selected = selected,
                selectionMode = selectionMode,
                inlineEditor = inlineEditor,
                enabled = enabled,
                onInlineValueChanged = onInlineValueChanged,
                onInlineSave = onInlineSave,
                onInlineReset = onInlineReset,
                onInlineDismiss = onInlineDismiss,
            )
        }
    }
}

@Composable
private fun BooleanFlagCardContent(
    flag: PhenotypeFlag,
    annotation: ServerFlagCatalogEntry?,
    value: Boolean,
    selected: Boolean,
    selectionMode: Boolean,
    enabled: Boolean,
    onBooleanChanged: (Boolean) -> Unit,
    onOverrideCleared: () -> Unit,
) {
    val isOn = flag.value.isEnabledBoolean()
    val hasDefault = flag.originalValue != null
    val controlsEnabled = !selectionMode && enabled

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = GmsDimensions.FlagCardMinHeight)
            .padding(horizontal = GmsSpacing.Large, vertical = GmsSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
    ) {
        SelectionIndicatorSlot(
            visible = selectionMode,
            selected = selected,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
        ) {
            FlagAnnotationContent(
                flagName = flag.name,
                annotation = annotation,
                nameStyle = MaterialTheme.typography.bodyLarge,
                nameColor = MaterialTheme.colorScheme.onSurface,
            )
            BooleanDefaultValueText(flag)
        }
        if (flag.overridden) {
            FilledTonalIconButton(
                onClick = onOverrideCleared,
                enabled = controlsEnabled,
                modifier = Modifier
                    .padding(start = GmsSpacing.Small)
                    .size(GmsBooleanControlSize),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
            ) {
                Icon(
                    imageVector = if (hasDefault) Icons.Rounded.Refresh else Icons.Rounded.Delete,
                    contentDescription = stringResource(
                        if (hasDefault) {
                            R.string.flag_reset_default_description
                        } else {
                            R.string.flag_delete_description
                        },
                    ),
                    modifier = Modifier.size(GmsDimensions.SelectionIconSize),
                )
            }
        }
        Switch(
            checked = isOn,
            onCheckedChange = { onBooleanChanged(!isOn) },
            enabled = controlsEnabled,
            thumbContent = if (flag.overridden) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(GmsSwitchThumbDotSize),
                    )
                }
            } else {
                null
            },
            colors = GmsSwitchColorsWithoutDisabledDimming(),
        )
    }
}

private val GmsBooleanControlSize = 32.dp

private val GmsSwitchThumbDotSize = 10.dp

@Composable
private fun GmsSwitchColorsWithoutDisabledDimming() = SwitchDefaults.colors().let { base ->
    base.copy(
        disabledCheckedThumbColor = base.checkedThumbColor,
        disabledCheckedTrackColor = base.checkedTrackColor,
        disabledCheckedBorderColor = base.checkedBorderColor,
        disabledCheckedIconColor = base.checkedIconColor,
        disabledUncheckedThumbColor = base.uncheckedThumbColor,
        disabledUncheckedTrackColor = base.uncheckedTrackColor,
        disabledUncheckedBorderColor = base.uncheckedBorderColor,
        disabledUncheckedIconColor = base.uncheckedIconColor,
    )
}

@Composable
private fun BooleanDefaultValueText(flag: PhenotypeFlag) {
    val defaultValue = flag.originalValue ?: return
    val isOn = defaultValue.isEnabledBoolean()
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(if (isOn) R.string.flag_value_true else R.string.flag_value_false),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
        )
    }
}

@Composable
private fun ValueFlagCardContent(
    flag: PhenotypeFlag,
    annotation: ServerFlagCatalogEntry?,
    selected: Boolean,
    selectionMode: Boolean,
    inlineEditor: InlineFlagEditor?,
    enabled: Boolean,
    onInlineValueChanged: (String) -> Unit,
    onInlineSave: () -> Unit,
    onInlineReset: () -> Unit,
    onInlineDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = GmsDimensions.FlagCardMinHeight)
            .padding(horizontal = GmsSpacing.ExtraLarge, vertical = GmsSpacing.Large),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionIndicatorSlot(
                    visible = selectionMode,
                    selected = selected,
                )
                FlagAnnotationContent(
                    flagName = flag.name,
                    annotation = annotation,
                    modifier = Modifier.weight(1f),
                    nameStyle = MaterialTheme.typography.labelLargeEmphasized,
                    nameColor = MaterialTheme.colorScheme.primary,
                )
            }

            if (flag.value.isEmpty()) {
                GmsEmptyValueBadge()
            } else {
                Text(
                    text = flag.value,
                    style = MaterialTheme.typography.bodyLargeEmphasized,
                    color = if (flag.overridden) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = if (flag.type == FlagType.String) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        InlineEditorAnimatedContent(
            editor = inlineEditor,
            enabled = enabled,
            onValueChanged = onInlineValueChanged,
            onSave = onInlineSave,
            onReset = onInlineReset,
            onDismiss = onInlineDismiss,
        )
    }
}

private val PreviewFlags = listOf(
    PhenotypeFlag(name = "unoverridden_default_on", type = FlagType.Boolean, originalValue = "1", value = "1", overridden = false),
    PhenotypeFlag(name = "unoverridden_default_off", type = FlagType.Boolean, originalValue = "0", value = "0", overridden = false),
    PhenotypeFlag(name = "overridden_forced_off", type = FlagType.Boolean, originalValue = "1", value = "0", overridden = true),
    PhenotypeFlag(name = "overridden_forced_on", type = FlagType.Boolean, originalValue = "0", value = "1", overridden = true),
    PhenotypeFlag(name = "no_default_value", type = FlagType.Boolean, originalValue = null, value = "1", overridden = true),
)

@Preview(showBackground = true)
@Composable
private fun FlagCardPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GmsSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            PreviewFlags.forEach { flag ->
                FlagCard(
                    flag = flag,
                    annotation = null,
                    selected = false,
                    selectionMode = false,
                    enabled = true,
                    onBooleanChanged = {},
                    onBooleanOverrideCleared = {},
                    inlineEditor = null,
                    onInlineValueChanged = {},
                    onInlineSave = {},
                    onInlineReset = {},
                    onInlineDismiss = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
private fun InlineEditorAnimatedContent(
    editor: InlineFlagEditor?,
    enabled: Boolean,
    onValueChanged: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val fastSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val fastEffectsSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    var lastEditor by remember { mutableStateOf(editor) }
    if (editor != null) lastEditor = editor

    AnimatedVisibility(
        visible = editor != null,
        modifier = Modifier.fillMaxWidth(),
        enter = expandVertically(animationSpec = spatialSpec) + fadeIn(effectsSpec),
        exit = shrinkVertically(animationSpec = fastSpatialSpec) + fadeOut(fastEffectsSpec),
    ) {
        lastEditor?.let { targetEditor ->
            InlineFlagEditorContent(
                editor = targetEditor,
                enabled = enabled,
                onValueChanged = onValueChanged,
                onSave = onSave,
                onReset = onReset,
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = GmsSpacing.Small),
            )
        }
    }
}

@Composable
private fun SelectionIndicatorSlot(
    visible: Boolean,
    selected: Boolean,
) {
    if (visible) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SelectionIndicator(selected)
            Spacer(Modifier.width(GmsSpacing.Medium))
        }
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(GmsDimensions.SelectionIndicatorSize)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier.border(
                        width = Dp.Hairline,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    )
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(GmsDimensions.SelectionIconSize),
            )
        }
    }
}
