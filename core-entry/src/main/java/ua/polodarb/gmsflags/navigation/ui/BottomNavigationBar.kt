package ua.polodarb.gmsflags.navigation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.navigation.model.BottomBarNavigationItem
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
internal fun BottomNavigationBar(
    items: List<BottomBarNavigationItem>,
    selectedDestination: BottomBarDestination,
    onDestinationSelected: (BottomBarDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        tonalElevation = GmsSpacing.None,
    ) {
        items.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = selectedDestination == item.destination,
                onClick = { onDestinationSelected(item.destination) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = label,
                    )
                },
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}
