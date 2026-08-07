package ua.polodarb.gmsflags.presentation.core.ui.layout

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import androidx.compose.foundation.layout.size

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GmsHeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    GmsHeaderActionButton(
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        selected = selected,
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GmsHeaderActionButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    FilledTonalIconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shapes = IconButtonDefaults.shapes(
            pressedShape = IconButtonDefaults.mediumPressedShape,
        ),
        modifier = modifier.size(GmsDimensions.DetailsHeaderActionSize),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        content = content,
    )
}
