package ua.polodarb.gmsflags.presentation.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

abstract class BaseViewModel<Event : ViewEvent, UiState : ViewState, Effect : ViewSideEffect> : ViewModel() {
    protected abstract fun initialState(): UiState
    protected abstract fun handleEvent(event: Event)

    private val mutableViewState by lazy { MutableStateFlow(initialState()) }
    val viewState: StateFlow<UiState> get() = mutableViewState.asStateFlow()
    private val effects = Channel<Effect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    fun setEvent(event: Event) {
        handleEvent(event)
    }

    protected fun setState(reducer: UiState.() -> UiState) { mutableViewState.update(reducer) }
    protected fun setEffect(builder: () -> Effect) {
        viewModelScope.launch { effects.send(builder()) }
    }
}
