package ua.polodarb.gmsflags.domain.onboarding

import kotlinx.coroutines.flow.Flow
import ua.polodarb.gmsflags.core.root.RootAccessManager
import ua.polodarb.gmsflags.data.repository.onboarding.OnboardingRepository

internal class ObserveOnboardingCompletionUseCase(
    private val repository: OnboardingRepository,
) : ObserveOnboardingCompletion {
    override fun invoke(): Flow<Boolean> = repository.observeCompleted()
}

internal class CompleteOnboardingUseCase(
    private val repository: OnboardingRepository,
) : CompleteOnboarding {
    override suspend fun invoke(): Result<Unit> = repository.markCompleted()
}

internal class RequestRootAccessUseCase(
    private val rootAccessManager: RootAccessManager,
) : RequestRootAccess {
    override suspend fun invoke(): Result<Unit> = rootAccessManager.requestAccess()
}
