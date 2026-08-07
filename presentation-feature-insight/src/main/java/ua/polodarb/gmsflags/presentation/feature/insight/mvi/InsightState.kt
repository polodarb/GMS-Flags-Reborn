package ua.polodarb.gmsflags.presentation.feature.insight.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

data class InsightState(
    val title: String = "GMS Insight",
    val message: String = "Inspection feature module",
) : ViewState
