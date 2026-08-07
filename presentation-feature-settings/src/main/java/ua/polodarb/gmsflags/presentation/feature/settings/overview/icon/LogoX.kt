package ua.polodarb.gmsflags.presentation.feature.settings.overview.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val LogoX: ImageVector
    get() {
        cachedLogoX?.let { return it }
        return ImageVector.Builder(
            name = "LogoX",
            defaultWidth = 300.dp,
            defaultHeight = 271.dp,
            viewportWidth = 300f,
            viewportHeight = 271f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveToRelative(236f, 0f)
                horizontalLineToRelative(46f)
                lineToRelative(-101f, 115f)
                lineToRelative(118f, 156f)
                horizontalLineToRelative(-92.6f)
                lineToRelative(-72.5f, -94.8f)
                lineToRelative(-83f, 94.8f)
                horizontalLineToRelative(-46f)
                lineToRelative(107f, -123f)
                lineToRelative(-113f, -148f)
                horizontalLineToRelative(94.9f)
                lineToRelative(65.5f, 86.6f)
                close()
                moveTo(219.9f, 244f)
                horizontalLineToRelative(25.5f)
                lineToRelative(-165f, -218f)
                horizontalLineToRelative(-27.4f)
                close()
            }
        }.build().also { cachedLogoX = it }
    }

private var cachedLogoX: ImageVector? = null
