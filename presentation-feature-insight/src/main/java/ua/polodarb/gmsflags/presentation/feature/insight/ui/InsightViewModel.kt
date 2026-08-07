package ua.polodarb.gmsflags.presentation.feature.insight.ui
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.feature.insight.mvi.InsightEffect
import ua.polodarb.gmsflags.presentation.feature.insight.mvi.InsightEvent
import ua.polodarb.gmsflags.presentation.feature.insight.mvi.InsightState
class InsightViewModel : BaseViewModel<InsightEvent, InsightState, InsightEffect>() {
    override fun initialState() = InsightState()
    override fun handleEvent(event: InsightEvent) = Unit
}
