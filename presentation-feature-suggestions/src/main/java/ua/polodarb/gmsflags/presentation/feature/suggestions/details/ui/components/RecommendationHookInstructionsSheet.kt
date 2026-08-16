@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Rule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.server.content.HookEffectDetails
import ua.polodarb.gmsflags.domain.server.content.HookEffectExpression
import ua.polodarb.gmsflags.domain.server.content.HookEffectKind
import ua.polodarb.gmsflags.domain.server.content.HookRecipeDetails
import ua.polodarb.gmsflags.domain.server.content.HookRuntimePoint
import ua.polodarb.gmsflags.domain.server.content.HookSelectorDetails
import ua.polodarb.gmsflags.domain.server.content.HookSelectorKind
import ua.polodarb.gmsflags.domain.server.content.HookValueSourceKind
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.shape.gmsGroupPositionOf
import ua.polodarb.gmsflags.presentation.core.ui.shape.gmsGroupedCardShape
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme
import ua.polodarb.gmsflags.presentation.feature.suggestions.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RecommendationHookInstructionsSheet(
    details: HookRecipeDetails,
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Small,
                ),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            StaggeredReveal(step = 0) {
                InstructionsHeader(details.effect)
            }
            StaggeredReveal(step = 1) {
                EffectExpressionContent(details.effect)
            }
            StaggeredReveal(step = 2) {
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                    Text(
                        text = stringResource(R.string.suggestions_details_hook_instructions_where_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    SelectorDetailsContent(details.selector)
                }
            }
            Button(
                onClick = hideSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GmsDimensions.PrimaryActionHeight),
            ) {
                Text(stringResource(R.string.suggestions_support_sheet_close))
            }
        }
    }
}

@Composable
private fun StaggeredReveal(step: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60L * step)
        visible = true
    }
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(effectsSpec) + slideInVertically(spatialSpec) { it / 4 },
    ) {
        content()
    }
}

@Composable
private fun InstructionsHeader(effect: HookEffectDetails) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val badgeShape = MaterialShapes.Cookie6Sided.toShape()
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(badgeShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.DataObject,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
        ) {
            Text(
                text = stringResource(R.string.suggestions_details_hook_instructions_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = effectSummary(effect),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun effectSummary(effect: HookEffectDetails): String {
    val point = stringResource(
        if (effect.hookPoint == HookRuntimePoint.BEFORE) {
            R.string.suggestions_details_hook_instructions_point_before
        } else {
            R.string.suggestions_details_hook_instructions_point_after
        }
    )
    val kind = stringResource(
        when (effect.kind) {
            HookEffectKind.BOOLEAN_RESULT -> R.string.suggestions_details_hook_instructions_kind_boolean
            HookEffectKind.NUMERIC_RESULT -> R.string.suggestions_details_hook_instructions_kind_numeric
            HookEffectKind.ARGUMENT_REPLACE -> R.string.suggestions_details_hook_instructions_kind_argument
            HookEffectKind.ARGUMENT_NULL -> R.string.suggestions_details_hook_instructions_kind_argument_null
            HookEffectKind.STRING_RESULT -> R.string.suggestions_details_hook_instructions_kind_string
            HookEffectKind.ADD_IMAGE_OVERLAY -> R.string.suggestions_details_hook_instructions_kind_image_overlay
        }
    )
    return "$point, $kind"
}

@Composable
private fun EffectExpressionContent(effect: HookEffectDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large)) {
        when (val expression = effect.expression) {
            is HookEffectExpression.Conditional -> RuleGroup(
                title = stringResource(R.string.suggestions_details_hook_instructions_applies_when),
                rules = expression.expression.toRuleSentences(),
                accent = MaterialTheme.colorScheme.primaryContainer,
                onAccent = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            is HookEffectExpression.ValueOnly -> Text(
                text = stringResource(
                    R.string.suggestions_details_hook_instructions_sets_to,
                    expression.toSentence(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )

            is HookEffectExpression.ImageOverlay -> ImageOverlayContent(expression)
            HookEffectExpression.None -> Unit
        }
        effect.whenExpression?.let { whenExpression ->
            RuleGroup(
                title = stringResource(R.string.suggestions_details_hook_instructions_only_when),
                rules = whenExpression.toRuleSentences(),
                accent = MaterialTheme.colorScheme.secondaryContainer,
                onAccent = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private val OverlayCheckerLight = Color(0xFFD0D0D0)
private val OverlayCheckerDark = Color(0xFFA6A6A6)

private fun Modifier.overlayCheckerboard(cellDp: Dp = 10.dp): Modifier = drawBehind {
    drawRect(OverlayCheckerLight)
    val cell = cellDp.toPx()
    val cols = ceil(size.width / cell).toInt()
    val rows = ceil(size.height / cell).toInt()
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            if ((row + col) % 2 == 1) {
                drawRect(
                    OverlayCheckerDark,
                    topLeft = Offset(col * cell, row * cell),
                    size = Size(cell, cell)
                )
            }
        }
    }
}

@Composable
private fun ImageOverlayContent(overlay: HookEffectExpression.ImageOverlay) {
    val imageBitmap = remember(overlay.imageBase64) {
        runCatching {
            val bytes = java.util.Base64.getDecoder().decode(overlay.imageBase64)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(MaterialTheme.shapes.medium)
                .overlayCheckerboard()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(72.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.suggestions_details_hook_instructions_overlay_undecodable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(GmsSpacing.Medium)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.shapes.medium
                        )
                        .padding(GmsSpacing.Medium)
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            SpecChip(overlay.gravity)
            SpecChip("${overlay.widthDp}×${overlay.heightDp} dp")
            if (overlay.offsetXDp != 0 || overlay.offsetYDp != 0) {
                SpecChip("offset ${overlay.offsetXDp}/${overlay.offsetYDp} dp")
            }
            SpecChip("${(overlay.alpha * 100).toInt()}%")
        }
    }
}

@Composable
private fun RuleGroup(
    title: String,
    rules: List<List<Pair<HookValueSourceKind, String>>>,
    accent: Color,
    onAccent: Color,
) {
    if (rules.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
            rules.forEachIndexed { index, conditions ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = gmsGroupedCardShape(gmsGroupPositionOf(index, rules.size)),
                    color = accent,
                    contentColor = onAccent,
                ) {
                    Column(
                        modifier = Modifier.padding(GmsSpacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    ) {
                        if (rules.size > 1) {
                            Text(
                                text = stringResource(
                                    R.string.suggestions_details_hook_instructions_rule,
                                    index + 1,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        conditions.forEach { (kind, sentence) ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(onAccent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = kind.icon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Text(text = sentence, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun HookValueSourceKind.icon(): ImageVector = when (this) {
    HookValueSourceKind.ORIGINAL_RESULT -> Icons.Outlined.Rule
    HookValueSourceKind.ARGUMENT -> Icons.Outlined.Numbers
    HookValueSourceKind.CONSTANT -> Icons.Outlined.Tag
    HookValueSourceKind.FLAG_OVERRIDE -> Icons.Outlined.Flag
    HookValueSourceKind.SYSTEM_FEATURE -> Icons.Outlined.Bolt
    HookValueSourceKind.SDK_INT -> Icons.Outlined.PhoneAndroid
    HookValueSourceKind.RESOURCE_ID -> Icons.Outlined.DataObject
}

@Composable
private fun SelectorDetailsContent(selector: HookSelectorDetails) {
    if (selector.kind == HookSelectorKind.VIEW_RESOURCE_ID) {
        Text(
            text = stringResource(
                R.string.suggestions_details_hook_instructions_where_view,
                "${selector.viewResourcePackage}:id/${selector.viewResourceName}",
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    if (selector.kind == HookSelectorKind.ANDROID_RESOURCE_STRING) {
        Text(
            text = stringResource(R.string.suggestions_details_hook_instructions_where_resource),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            SpecChip(
                stringResource(
                    R.string.suggestions_details_hook_instructions_returns,
                    selector.methodReturnType.ifBlank { "?" },
                )
            )
            SpecChip(
                if (selector.methodParameterTypes.isEmpty()) {
                    stringResource(R.string.suggestions_details_hook_instructions_params_none)
                } else {
                    stringResource(
                        R.string.suggestions_details_hook_instructions_params,
                        selector.methodParameterTypes.joinToString(", "),
                    )
                }
            )
            if (selector.methodModifiersAll.isNotEmpty()) {
                SpecChip(
                    stringResource(
                        R.string.suggestions_details_hook_instructions_modifiers,
                        selector.methodModifiersAll.joinToString(", "),
                    )
                )
            }
        }
        val referenceLines = buildList {
            if (selector.classUsingStringsAll.isNotEmpty()) {
                add(
                    stringResource(
                        R.string.suggestions_details_hook_instructions_class_references_all,
                        selector.classUsingStringsAll.joinToString(", ") { "\"$it\"" },
                    )
                )
            }
            if (selector.classUsingStringsAny.isNotEmpty()) {
                add(
                    stringResource(
                        R.string.suggestions_details_hook_instructions_class_references_any,
                        selector.classUsingStringsAny.joinToString(", ") { "\"$it\"" },
                    )
                )
            }
            if (selector.methodUsingStringsAll.isNotEmpty()) {
                add(
                    stringResource(
                        R.string.suggestions_details_hook_instructions_references_all,
                        selector.methodUsingStringsAll.joinToString(", ") { "\"$it\"" },
                    )
                )
            }
            if (selector.methodUsingStringsAny.isNotEmpty()) {
                add(
                    stringResource(
                        R.string.suggestions_details_hook_instructions_references_any,
                        selector.methodUsingStringsAny.joinToString(", ") { "\"$it\"" },
                    )
                )
            }
        }
        if (referenceLines.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Column(
                    modifier = Modifier.padding(GmsSpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    referenceLines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Small,
                vertical = GmsSpacing.ExtraSmall
            ),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private const val SAMPLE_OVERLAY_PNG_BASE64 =
    "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAIAAADYYG7QAAAAxElEQVR4nO2WMRLCQAwDFQ1vcAmP" +
        "g2fxOUr8CnoofHZyGR3jLTPnSKfYE2/v6x1KEGIQYhBiEGIQYhBiEGIQYhBiEGIQYhBiEGJcdtbb" +
        "6/n1xG+PPS/cyuuH/Vg5xBZnuMHAgSMN2ZhYzVPakGVkCp5yhiwvkC2RG3uOH7Vqn6YKV07oHNrQ" +
        "Pyfk1d9TqnDlhFAKKVvCqQKev0Dlk/mYTK3nij3kkVh5Auobo9wKO4nFx/4E2lBEJxTRCUV0QhGd" +
        "UIRcQh9gEi0HclbYUgAAAABJRU5ErkJggg=="

private fun sampleImageOverlay() = HookEffectExpression.ImageOverlay(
    imageBase64 = SAMPLE_OVERLAY_PNG_BASE64,
    gravity = "TOP_LEFT",
    widthDp = 24,
    heightDp = 24,
    offsetXDp = 0,
    offsetYDp = 0,
    alpha = 1f,
)

@Preview(name = "Overlay effect", showBackground = true, widthDp = 360)
@Composable
private fun ImageOverlayContentPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp)) { ImageOverlayContent(sampleImageOverlay()) }
        }
    }
}

@Preview(name = "Overlay effect · dark", showBackground = true, widthDp = 360)
@Composable
private fun ImageOverlayContentDarkPreview() {
    GMSFlags20Theme(darkTheme = true, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp)) { ImageOverlayContent(sampleImageOverlay()) }
        }
    }
}

@Preview(name = "Overlay target selector", showBackground = true, widthDp = 360)
@Composable
private fun ViewResourceSelectorPreview() {
    GMSFlags20Theme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp)) {
                SelectorDetailsContent(
                    HookSelectorDetails(
                        kind = HookSelectorKind.VIEW_RESOURCE_ID,
                        classUsingStringsAll = emptyList(),
                        classUsingStringsAny = emptyList(),
                        methodReturnType = "",
                        methodParameterTypes = emptyList(),
                        methodModifiersAll = emptyList(),
                        methodUsingStringsAll = emptyList(),
                        methodUsingStringsAny = emptyList(),
                        viewResourceName = "key_pos_space",
                        viewResourcePackage = "com.google.android.inputmethod.latin",
                    ),
                )
            }
        }
    }
}
