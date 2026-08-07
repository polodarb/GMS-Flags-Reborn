package ua.polodarb.gmsflags.data.repository.impl.flags

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ua.polodarb.gmsflags.data.repository.flags.FlagOverridesChangeBus
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange

internal class FlagOverridesChangeBusImpl : FlagOverridesChangeBus {
    private val mutableChanges = MutableSharedFlow<FlagOverridesChange>()
    override val changes: SharedFlow<FlagOverridesChange> = mutableChanges.asSharedFlow()

    override suspend fun notifyChanged(change: FlagOverridesChange) {
        mutableChanges.emit(change)
    }
}
