package ua.polodarb.gmsflags.presentation.feature.settings.faq

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.server.content.GetFaq
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel

internal class FaqViewModel(
    private val getFaq: GetFaq,
    private val errorResolver: ErrorResolver,
) : BaseViewModel<FaqEvent, FaqState, FaqEffect>() {
    private var loadJob: Job? = null

    override fun initialState() = FaqState()

    init { load() }

    override fun handleEvent(event: FaqEvent) {
        when (event) {
            FaqEvent.Retry -> load()
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            setState { copy(loading = entries.isEmpty(), error = null) }
            getFaq().fold(
                onSuccess = { entries ->
                    setState { copy(loading = false, entries = entries, error = null) }
                },
                onFailure = { failure ->
                    setState { copy(loading = false, error = errorResolver.resolve(failure)) }
                },
            )
        }
    }
}
