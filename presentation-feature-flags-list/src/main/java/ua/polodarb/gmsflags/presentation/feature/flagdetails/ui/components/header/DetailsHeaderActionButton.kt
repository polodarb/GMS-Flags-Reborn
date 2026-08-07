package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsHeaderActionButton

@Composable
internal fun DetailsHeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    GmsHeaderActionButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        selected = selected,
    )
}
