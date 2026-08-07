package ua.polodarb.gmsflags.data.repository.flags

import kotlinx.coroutines.flow.SharedFlow
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange

interface FlagOverridesChangeBus {
    val changes: SharedFlow<FlagOverridesChange>

    /** Called by the repository after any successful override write. */
    suspend fun notifyChanged(change: FlagOverridesChange)
}
