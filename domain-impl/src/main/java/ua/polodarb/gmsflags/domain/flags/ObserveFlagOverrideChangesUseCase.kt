package ua.polodarb.gmsflags.domain.flags

import kotlinx.coroutines.flow.Flow
import ua.polodarb.gmsflags.data.repository.flags.FlagOverridesChangeBus

class ObserveFlagOverrideChangesUseCase(
    private val changeBus: FlagOverridesChangeBus,
) : ObserveFlagOverrideChanges {
    override fun invoke(): Flow<FlagOverridesChange> = changeBus.changes
}
