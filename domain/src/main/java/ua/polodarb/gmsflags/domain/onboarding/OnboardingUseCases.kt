package ua.polodarb.gmsflags.domain.onboarding

import kotlinx.coroutines.flow.Flow

fun interface ObserveOnboardingCompletion {
    operator fun invoke(): Flow<Boolean>
}

fun interface CompleteOnboarding {
    suspend operator fun invoke(): Result<Unit>
}

fun interface RequestRootAccess {
    suspend operator fun invoke(): Result<Unit>
}
