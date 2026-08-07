package ua.polodarb.gmsflags.presentation.feature.insight.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IcLogo: ImageVector
    get() {
        if (_IcLogo != null) {
            return _IcLogo!!
        }
        _IcLogo = ImageVector.Builder(
            name = "IcLogo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 689f,
            viewportHeight = 689f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(458.5f, 516.75f)
                curveTo(482.98f, 516.75f, 500.05f, 540.95f, 486.08f, 561.05f)
                curveTo(454.98f, 605.85f, 403.16f, 635.17f, 344.5f, 635.17f)
                curveTo(285.84f, 635.17f, 234.02f, 605.85f, 202.92f, 561.05f)
                curveTo(188.95f, 540.95f, 206.02f, 516.75f, 230.5f, 516.75f)
                curveTo(243.34f, 516.75f, 254.86f, 523.92f, 263.29f, 533.6f)
                curveTo(283.03f, 556.26f, 312.09f, 570.58f, 344.5f, 570.58f)
                curveTo(376.91f, 570.58f, 405.97f, 556.26f, 425.71f, 533.6f)
                curveTo(434.14f, 523.92f, 445.66f, 516.75f, 458.5f, 516.75f)
                close()
                moveTo(505.98f, 75.36f)
                curveTo(607.06f, 75.36f, 689f, 157.3f, 689f, 258.38f)
                curveTo(689f, 359.45f, 607.06f, 441.39f, 505.98f, 441.39f)
                horizontalLineTo(183.02f)
                curveTo(81.94f, 441.39f, 0f, 359.45f, 0f, 258.38f)
                curveTo(0f, 157.3f, 81.94f, 75.36f, 183.02f, 75.36f)
                horizontalLineTo(505.98f)
                close()
                moveTo(219f, 174f)
                curveTo(204.64f, 174f, 193f, 185.64f, 193f, 200f)
                verticalLineTo(227f)
                horizontalLineTo(166f)
                curveTo(151.64f, 227f, 140f, 238.64f, 140f, 253f)
                verticalLineTo(260f)
                curveTo(140f, 274.36f, 151.64f, 286f, 166f, 286f)
                horizontalLineTo(193f)
                verticalLineTo(313f)
                curveTo(193f, 327.36f, 204.64f, 339f, 219f, 339f)
                horizontalLineTo(226f)
                curveTo(240.36f, 339f, 252f, 327.36f, 252f, 313f)
                verticalLineTo(286f)
                horizontalLineTo(279f)
                curveTo(293.36f, 286f, 305f, 274.36f, 305f, 260f)
                verticalLineTo(253f)
                curveTo(305f, 238.64f, 293.36f, 227f, 279f, 227f)
                horizontalLineTo(252f)
                verticalLineTo(200f)
                curveTo(252f, 185.64f, 240.36f, 174f, 226f, 174f)
                horizontalLineTo(219f)
                close()
                moveTo(428.5f, 229.5f)
                curveTo(413.59f, 229.5f, 401.5f, 241.59f, 401.5f, 256.5f)
                curveTo(401.5f, 271.41f, 413.59f, 283.5f, 428.5f, 283.5f)
                horizontalLineTo(539.5f)
                curveTo(554.41f, 283.5f, 566.5f, 271.41f, 566.5f, 256.5f)
                curveTo(566.5f, 241.59f, 554.41f, 229.5f, 539.5f, 229.5f)
                horizontalLineTo(428.5f)
                close()
            }
        }.build()

        return _IcLogo!!
    }

@Suppress("ObjectPropertyName")
private var _IcLogo: ImageVector? = null
