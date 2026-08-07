package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
internal fun ExpandableIconSlot(
    collapsedHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.height(collapsedHeight),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
