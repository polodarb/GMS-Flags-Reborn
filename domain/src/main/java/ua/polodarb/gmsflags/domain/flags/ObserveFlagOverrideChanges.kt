package ua.polodarb.gmsflags.domain.flags

import kotlinx.coroutines.flow.Flow

/** Emits whenever flag overrides change, so snapshot screens can refresh. */
fun interface ObserveFlagOverrideChanges {
    operator fun invoke(): Flow<FlagOverridesChange>
}
