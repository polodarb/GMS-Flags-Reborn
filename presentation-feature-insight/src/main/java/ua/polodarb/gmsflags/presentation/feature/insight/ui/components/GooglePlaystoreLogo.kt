package ua.polodarb.gmsflags.presentation.feature.insight.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val GooglePlaystoreLogo: ImageVector
    get() {
        if (_GooglePlaystoreLogo != null) {
            return _GooglePlaystoreLogo!!
        }
        _GooglePlaystoreLogo = ImageVector.Builder(
            name = "GooglePlaystoreLogo",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(48f, 59.49f)
                verticalLineToRelative(393f)
                arcToRelative(4.33f, 4.33f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.37f, 3.07f)
                lineTo(260f, 256f)
                lineTo(55.37f, 56.42f)
                arcTo(4.33f, 4.33f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 59.49f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(345.8f, 174f)
                lineTo(89.22f, 32.64f)
                lineToRelative(-0.16f, -0.09f)
                curveToRelative(-4.42f, -2.4f, -8.62f, 3.58f, -5f, 7.06f)
                lineTo(285.19f, 231.93f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(84.08f, 472.39f)
                curveToRelative(-3.64f, 3.48f, 0.56f, 9.46f, 5f, 7.06f)
                lineToRelative(0.16f, -0.09f)
                lineTo(345.8f, 338f)
                lineToRelative(-60.61f, -57.95f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(449.38f, 231f)
                lineToRelative(-71.65f, -39.46f)
                lineTo(310.36f, 256f)
                lineToRelative(67.37f, 64.43f)
                lineTo(449.38f, 281f)
                curveTo(468.87f, 270.23f, 468.87f, 241.77f, 449.38f, 231f)
                close()
            }
        }.build()

        return _GooglePlaystoreLogo!!
    }

@Suppress("ObjectPropertyName")
private var _GooglePlaystoreLogo: ImageVector? = null
