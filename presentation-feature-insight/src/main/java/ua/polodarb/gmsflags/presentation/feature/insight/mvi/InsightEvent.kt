package ua.polodarb.gmsflags.presentation.feature.insight.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent

sealed interface InsightEvent : ViewEvent {
    data object ScreenOpened : InsightEvent
}
