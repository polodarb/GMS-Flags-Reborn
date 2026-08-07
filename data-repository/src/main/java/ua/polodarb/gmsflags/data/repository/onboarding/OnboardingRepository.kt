package ua.polodarb.gmsflags.data.repository.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun observeCompleted(): Flow<Boolean>

    suspend fun markCompleted(): Result<Unit>
}
