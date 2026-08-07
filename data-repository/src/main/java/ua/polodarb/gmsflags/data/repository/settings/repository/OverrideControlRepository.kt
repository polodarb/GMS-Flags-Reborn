package ua.polodarb.gmsflags.data.repository.settings.repository

import kotlinx.coroutines.flow.StateFlow
import ua.polodarb.gmsflags.domain.settings.OverrideControlState

interface OverrideControlRepository {
    val state: StateFlow<OverrideControlState>
    suspend fun refresh(): Result<Unit>
    suspend fun setPaused(paused: Boolean): Result<Unit>
    suspend fun deleteAll(): Result<Unit>
    suspend fun deleteAll(androidPackageName: String): Result<Unit>
}
