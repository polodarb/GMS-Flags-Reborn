package ua.polodarb.gmsflags.presentation.core.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GmsNavigationType {
    BottomBar,
    Rail,
}

@Immutable
data class GmsAdaptiveLayout(
    val contentPadding: Dp,
    val contentMaxWidth: Dp,
    val listMaxWidth: Dp,
    val applicationCardMinWidth: Dp,
    val recommendationCardMinWidth: Dp,
    val flagCardMinWidth: Dp,
    val navigationType: GmsNavigationType,
)

object GmsAdaptiveLayoutDefaults {
    fun from(widthSizeClass: WindowWidthSizeClass): GmsAdaptiveLayout = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Compact
        WindowWidthSizeClass.Medium -> Medium
        else -> Expanded
    }

    val Compact = GmsAdaptiveLayout(
        contentPadding = GmsSpacing.Large,
        contentMaxWidth = 840.dp,
        listMaxWidth = 720.dp,
        applicationCardMinWidth = 320.dp,
        recommendationCardMinWidth = 300.dp,
        flagCardMinWidth = 300.dp,
        navigationType = GmsNavigationType.BottomBar,
    )

    val Medium = GmsAdaptiveLayout(
        contentPadding = GmsSpacing.ExtraLarge,
        contentMaxWidth = 1040.dp,
        listMaxWidth = 840.dp,
        applicationCardMinWidth = 340.dp,
        recommendationCardMinWidth = 340.dp,
        flagCardMinWidth = 320.dp,
        navigationType = GmsNavigationType.Rail,
    )

    val Expanded = GmsAdaptiveLayout(
        contentPadding = GmsSpacing.ExtraLarge,
        contentMaxWidth = 1040.dp,
        listMaxWidth = 920.dp,
        applicationCardMinWidth = 360.dp,
        recommendationCardMinWidth = 360.dp,
        flagCardMinWidth = 340.dp,
        navigationType = GmsNavigationType.Rail,
    )
}

val LocalGmsAdaptiveLayout = staticCompositionLocalOf {
    GmsAdaptiveLayoutDefaults.Compact
}
