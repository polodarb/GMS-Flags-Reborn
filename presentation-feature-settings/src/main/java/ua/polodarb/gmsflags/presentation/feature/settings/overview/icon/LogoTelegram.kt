package ua.polodarb.gmsflags.presentation.feature.settings.overview.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val LogoTelegram: ImageVector
    get() {
        cachedLogoTelegram?.let { return it }
        return ImageVector.Builder(
            name = "LogoTelegram",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(477f, 43.86f)
                lineTo(13.32f, 223.29f)
                arcToRelative(5.86f, 5.86f, 0f, false, false, -0.8f, 0.38f)
                curveToRelative(-3.76f, 2.13f, -30f, 18.18f, 7f, 32.57f)
                lineToRelative(110.79f, 35.81f)
                arcToRelative(6.08f, 6.08f, 0f, false, false, 5.09f, -0.62f)
                lineTo(409.25f, 120.57f)
                arcToRelative(6f, 6f, 0f, false, true, 2.2f, -0.83f)
                curveToRelative(3.81f, -0.63f, 14.78f, -1.81f, 7.84f, 7f)
                curveToRelative(-7.85f, 10f, -194.9f, 177.62f, -215.66f, 196.21f)
                arcToRelative(6.3f, 6.3f, 0f, false, false, -2.07f, 4.17f)
                lineToRelative(-9.06f, 108f)
                arcToRelative(7.08f, 7.08f, 0f, false, false, 2.83f, 5.67f)
                arcToRelative(6.88f, 6.88f, 0f, false, false, 8.17f, -0.62f)
                lineToRelative(65.6f, -58.63f)
                arcToRelative(6.09f, 6.09f, 0f, false, true, 7.63f, -0.39f)
                lineToRelative(114.82f, 83.35f)
                curveToRelative(2.77f, 1.71f, 32.69f, 19.12f, 41.33f, -19.76f)
                lineToRelative(79f, -375.65f)
                curveToRelative(0.11f, -1.19f, 1.18f, -14.27f, -8.17f, -22f)
                curveToRelative(-9.82f, -8.08f, -23.72f, -4f, -25.81f, -3.56f)
                arcTo(6f, 6f, 0f, false, false, 477f, 43.86f)
                close()
            }
        }.build().also { cachedLogoTelegram = it }
    }

private var cachedLogoTelegram: ImageVector? = null
