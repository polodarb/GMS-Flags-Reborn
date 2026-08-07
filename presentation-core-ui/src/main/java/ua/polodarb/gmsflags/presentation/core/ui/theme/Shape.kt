package ua.polodarb.gmsflags.presentation.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

val GmsShapes = Shapes(
    small = RoundedCornerShape(GmsSpacing.Small),
    medium = RoundedCornerShape(GmsSpacing.Medium),
    large = RoundedCornerShape(GmsSpacing.ExtraLarge),
    extraLarge = RoundedCornerShape(GmsSpacing.Huge),
)

/**
 * Shape for elements nested one level inside a card that uses [GmsShapes.large] (a recommendation
 * card's logo, its warning/info blocks).
 */
val GmsNestedCardShape = RoundedCornerShape(16.dp)
