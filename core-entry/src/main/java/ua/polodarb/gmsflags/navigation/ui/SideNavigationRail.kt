package ua.polodarb.gmsflags.navigation.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.navigation.model.BottomBarNavigationItem
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination

@Composable
internal fun SideNavigationRail(
    items: List<BottomBarNavigationItem>,
    selectedDestination: BottomBarDestination,
    onDestinationSelected: (BottomBarDestination) -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
    ) {
        items.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationRailItem(
                selected = selectedDestination == item.destination,
                onClick = { onDestinationSelected(item.destination) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}
