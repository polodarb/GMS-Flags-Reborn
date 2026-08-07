package ua.polodarb.gmsflags.presentation.core.ui.overscroll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

/**
 * App-wide rubber-band strength for the Cupertino overscroll.
 */
const val GmsOverscrollRubberBandCoefficient = 0.385f

/**
 * Factory for creating [CupertinoOverscrollEffect] instances with iOS-like rubber band physics.
 *
 * @param density Display density taken into consideration during computations; Cupertino formulas
 * use DPs while the scroll machinery uses raw pixels.
 * @param applyClip Whether the effect should clip its content. Some consumers apply clipping
 * themselves, so this flag keeps the modifier chain correct in every case while avoiding redundancy.
 * @param topOverscrollEnabled When false, the leading (top) vertical overscroll is disabled so a
 * top pull is left to the parent (e.g. pull-to-refresh).
 */
@OptIn(ExperimentalFoundationApi::class)
data class CupertinoOverscrollFactory(
    private val density: Float,
    private val applyClip: Boolean = false,
    private val rubberBandCoefficient: Float =
        CupertinoOverscrollEffect.DEFAULT_RUBBER_BAND_COEFFICIENT,
    private val topOverscrollEnabled: Boolean = true,
) : OverscrollFactory {

    override fun createOverscrollEffect(): OverscrollEffect {
        return CupertinoOverscrollEffect(
            density = density,
            applyClip = applyClip,
            rubberBandCoefficient = rubberBandCoefficient,
            topOverscrollEnabled = topOverscrollEnabled,
        )
    }
}

/**
 * Composable function to create and remember a [CupertinoOverscrollEffect] using the current density.
 */
@Composable
fun rememberCupertinoOverscrollEffect(
    applyClip: Boolean = false,
): OverscrollEffect {
    val density = LocalDensity.current.density
    return remember(density, applyClip) {
        CupertinoOverscrollEffect(
            density = density,
            applyClip = applyClip,
        )
    }
}

/**
 * Composable function to create and remember a [CupertinoOverscrollFactory] using the current density.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberCupertinoOverscrollFactory(
    applyClip: Boolean = false,
    rubberBandCoefficient: Float = CupertinoOverscrollEffect.DEFAULT_RUBBER_BAND_COEFFICIENT,
    topOverscrollEnabled: Boolean = true,
): OverscrollFactory {
    val density = LocalDensity.current.density
    return remember(density, applyClip, rubberBandCoefficient, topOverscrollEnabled) {
        CupertinoOverscrollFactory(
            density = density,
            applyClip = applyClip,
            rubberBandCoefficient = rubberBandCoefficient,
            topOverscrollEnabled = topOverscrollEnabled,
        )
    }
}
