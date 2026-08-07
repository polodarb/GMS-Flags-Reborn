package ua.polodarb.gmsflags.presentation.core.ui.theme

import androidx.compose.material3.ColorScheme

internal fun ColorScheme.withDarkSurfaceHierarchy(): ColorScheme = copy(
    surfaceDim = surfaceBright,
    surfaceBright = surfaceDim,
    surfaceContainerLow = surfaceContainerLowest,
    surfaceContainerHigh = surfaceContainerLow,
)
