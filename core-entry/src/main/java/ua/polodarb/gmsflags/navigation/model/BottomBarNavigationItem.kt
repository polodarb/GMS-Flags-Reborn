package ua.polodarb.gmsflags.navigation.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import ua.polodarb.gmsflags.R
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.feature.insight.ui.components.IcLogo

@Stable
@Immutable
internal data class BottomBarNavigationItem(
    val destination: BottomBarDestination,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

internal object BottomBarNavigation {
    fun items(offline: Boolean): List<BottomBarNavigationItem> {
        val apps = BottomBarNavigationItem(
            destination = BottomBarDestination.Apps,
            labelRes = R.string.navigation_apps,
            icon = Icons.Rounded.Apps,
        )
        if (offline) return listOf(apps)
        return listOf(
            BottomBarNavigationItem(
                destination = BottomBarDestination.Suggestions,
                labelRes = R.string.navigation_suggestions,
                icon = Icons.Rounded.AutoAwesome,
            ),
            apps,
            BottomBarNavigationItem(
                destination = BottomBarDestination.GmsInsight,
                labelRes = R.string.navigation_gms_insight,
                icon = IcLogo,
            ),
        ) + experimentalBottomBarNavigationItems()
    }
}
