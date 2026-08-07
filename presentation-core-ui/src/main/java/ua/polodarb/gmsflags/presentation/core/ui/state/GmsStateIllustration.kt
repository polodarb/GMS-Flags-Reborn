package ua.polodarb.gmsflags.presentation.core.ui.state

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
internal fun gmsStateIllustration(): ImageVector {
    val secondary = MaterialTheme.colorScheme.secondary
    return remember(secondary) {
        ImageVector.Builder(
            name = "Property1Default",
            defaultWidth = 900.dp,
            defaultHeight = 322.dp,
            viewportWidth = 900f,
            viewportHeight = 322f
        ).apply {
            path(
                fill = SolidColor(secondary),
                fillAlpha = 0.15f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(133.16f, 289.29f)
                curveTo(133.16f, 307.04f, 147.56f, 321.43f, 165.31f, 321.43f)
                horizontalLineTo(263.82f)
                curveTo(216.77f, 321.43f, 178.62f, 284.15f, 178.62f, 238.17f)
                curveTo(178.62f, 192.18f, 216.77f, 154.9f, 263.82f, 154.9f)
                curveTo(265.82f, 154.9f, 267.8f, 154.97f, 269.77f, 155.1f)
                curveTo(268.4f, 147.5f, 267.69f, 139.67f, 267.69f, 131.67f)
                curveTo(267.69f, 58.95f, 326.64f, 0f, 399.36f, 0f)
                curveTo(457.42f, 0f, 506.7f, 37.57f, 524.21f, 89.73f)
                curveTo(528.36f, 89.29f, 532.58f, 89.07f, 536.84f, 89.07f)
                curveTo(602.07f, 89.07f, 654.96f, 141.09f, 654.96f, 205.25f)
                curveTo(654.96f, 266.21f, 603.52f, 321.34f, 546.52f, 321.34f)
                curveTo(489.52f, 321.34f, 546.52f, 321.43f, 546.52f, 321.43f)
                horizontalLineTo(782.14f)
                curveTo(799.89f, 321.43f, 814.28f, 307.04f, 814.28f, 289.29f)
                curveTo(814.28f, 271.53f, 799.89f, 257.14f, 782.14f, 257.14f)
                horizontalLineTo(754.59f)
                curveTo(736.83f, 257.14f, 722.44f, 242.75f, 722.44f, 225f)
                curveTo(722.44f, 207.25f, 736.83f, 192.86f, 754.59f, 192.86f)
                horizontalLineTo(841.83f)
                curveTo(859.58f, 192.86f, 873.97f, 178.47f, 873.97f, 160.71f)
                curveTo(873.97f, 142.96f, 859.58f, 128.57f, 841.83f, 128.57f)
                horizontalLineTo(740.81f)
                curveTo(758.56f, 128.57f, 772.95f, 114.18f, 772.95f, 96.43f)
                curveTo(772.95f, 78.68f, 758.56f, 64.29f, 740.81f, 64.29f)
                horizontalLineTo(537.24f)
                curveTo(555f, 64.29f, 569.39f, 49.89f, 569.39f, 32.14f)
                curveTo(569.39f, 14.39f, 555f, 0f, 537.24f, 0f)
                horizontalLineTo(399.36f)
                horizontalLineTo(238.78f)
                curveTo(221.02f, 0f, 206.63f, 14.39f, 206.63f, 32.14f)
                curveTo(206.63f, 49.89f, 221.02f, 64.29f, 238.78f, 64.29f)
                horizontalLineTo(104.55f)
                curveTo(86.8f, 64.29f, 72.41f, 78.68f, 72.41f, 96.43f)
                curveTo(72.41f, 114.18f, 86.8f, 128.57f, 104.55f, 128.57f)
                horizontalLineTo(151.53f)
                curveTo(169.28f, 128.57f, 183.68f, 142.96f, 183.68f, 160.71f)
                curveTo(183.68f, 178.47f, 169.28f, 192.86f, 151.53f, 192.86f)
                horizontalLineTo(35.68f)
                curveTo(17.92f, 192.86f, 3.53f, 207.25f, 3.53f, 225f)
                curveTo(3.53f, 242.75f, 17.92f, 257.14f, 35.68f, 257.14f)
                horizontalLineTo(165.31f)
                curveTo(147.56f, 257.14f, 133.16f, 271.53f, 133.16f, 289.29f)
                close()
            }
            path(
                fill = SolidColor(secondary),
                fillAlpha = 0.15f
            ) {
                moveTo(867.42f, 321.43f)
                curveTo(885.17f, 321.43f, 899.56f, 307.04f, 899.56f, 289.29f)
                curveTo(899.56f, 271.53f, 885.17f, 257.14f, 867.42f, 257.14f)
                curveTo(849.66f, 257.14f, 835.27f, 271.53f, 835.27f, 289.29f)
                curveTo(835.27f, 307.04f, 849.66f, 321.43f, 867.42f, 321.43f)
                close()
            }
            path(
                stroke = SolidColor(secondary),
                strokeAlpha = 0.1f,
                strokeLineWidth = 8.46939f
            ) {
                moveTo(624.92f, 39.95f)
                curveTo(632.53f, 39.95f, 638.69f, 33.78f, 638.69f, 26.17f)
                curveTo(638.69f, 18.57f, 632.53f, 12.4f, 624.92f, 12.4f)
                curveTo(617.31f, 12.4f, 611.14f, 18.57f, 611.14f, 26.17f)
                curveTo(611.14f, 33.78f, 617.31f, 39.95f, 624.92f, 39.95f)
                close()
            }
            path(
                fill = SolidColor(secondary),
                fillAlpha = 0.15f
            ) {
                moveTo(763.26f, 237.89f)
                curveTo(770.86f, 237.89f, 777.03f, 231.73f, 777.03f, 224.12f)
                curveTo(777.03f, 216.51f, 770.86f, 210.34f, 763.26f, 210.34f)
                curveTo(755.65f, 210.34f, 749.48f, 216.51f, 749.48f, 224.12f)
                curveTo(749.48f, 231.73f, 755.65f, 237.89f, 763.26f, 237.89f)
                close()
            }
            path(
                stroke = SolidColor(secondary),
                strokeAlpha = 0.1f,
                strokeLineWidth = 10.5867f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(666.51f, 95.05f)
                lineTo(704.9f, 133.43f)
                moveTo(705.48f, 95.05f)
                lineTo(667.1f, 133.43f)
            }
            path(
                stroke = SolidColor(secondary),
                strokeAlpha = 0.1f,
                strokeLineWidth = 10.5867f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(151.33f, 5.99f)
                lineTo(178.88f, 33.54f)
                moveTo(178.88f, 5.99f)
                lineTo(151.33f, 33.54f)
            }
            path(
                fill = SolidColor(secondary),
                fillAlpha = 0.15f
            ) {
                moveTo(137.56f, 173.27f)
                curveTo(145.17f, 173.27f, 151.33f, 167.11f, 151.33f, 159.5f)
                curveTo(151.33f, 151.89f, 145.17f, 145.72f, 137.56f, 145.72f)
                curveTo(129.95f, 145.72f, 123.78f, 151.89f, 123.78f, 159.5f)
                curveTo(123.78f, 167.11f, 129.95f, 173.27f, 137.56f, 173.27f)
                close()
            }
            path(
                fill = SolidColor(secondary),
                fillAlpha = 0.15f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(353.37f, 54.27f)
                curveTo(353.37f, 157.6f, 463.87f, 265.84f, 601.59f, 265.84f)
                curveTo(584.72f, 284.31f, 558.66f, 296.83f, 528.97f, 298.8f)
                verticalLineTo(298.86f)
                horizontalLineTo(287.14f)
                curveTo(257.8f, 298.86f, 208.79f, 287.86f, 208.79f, 239.69f)
                curveTo(208.79f, 191.53f, 251.38f, 180.53f, 291.63f, 180.53f)
                curveTo(293.34f, 180.53f, 295.04f, 180.57f, 296.72f, 180.67f)
                curveTo(295.55f, 175.26f, 295.35f, 169.69f, 294.94f, 164.01f)
                curveTo(292.21f, 125.65f, 297.55f, 65.7f, 353.43f, 49.96f)
                curveTo(353.39f, 51.4f, 353.37f, 52.83f, 353.37f, 54.27f)
                close()
            }
            path(
                stroke = SolidColor(secondary),
                strokeAlpha = 0.1f,
                strokeLineWidth = 10.5867f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(400.8f, 266.63f)
                curveTo(412.54f, 266.63f, 422.06f, 257.11f, 422.06f, 245.37f)
                curveTo(422.06f, 233.64f, 412.54f, 224.12f, 400.8f, 224.12f)
                curveTo(389.07f, 224.12f, 379.55f, 233.64f, 379.55f, 245.37f)
                curveTo(379.55f, 257.11f, 389.07f, 266.63f, 400.8f, 266.63f)
                close()
            }
            path(
                stroke = SolidColor(secondary),
                strokeAlpha = 0.1f,
                strokeLineWidth = 10.5867f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(340.91f, 200.93f)
                lineTo(367.96f, 179.71f)
                lineTo(340.91f, 159.5f)
            }
            path(
                stroke = SolidColor(secondary),
                strokeAlpha = 0.1f,
                strokeLineWidth = 10.5867f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(460.69f, 200.93f)
                lineTo(433.65f, 179.71f)
                lineTo(460.69f, 159.5f)
            }
            path(
                stroke = SolidColor(secondary),
                strokeAlpha = 0.1f,
                strokeLineWidth = 12.7041f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(433.65f, 43.68f)
                curveTo(458.8f, 50.34f, 478.66f, 70.01f, 485.58f, 95.05f)
            }
        }.build()
    }
}
