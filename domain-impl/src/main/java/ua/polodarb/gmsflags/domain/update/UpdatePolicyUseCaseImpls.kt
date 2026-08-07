package ua.polodarb.gmsflags.domain.update

import ua.polodarb.gmsflags.data.repository.update.UpdatePolicyRepository

class GetAppUpdatePolicyUseCase(
    private val repository: UpdatePolicyRepository,
) : GetAppUpdatePolicy {
    override suspend fun invoke(): AppUpdatePolicy? = repository.getUpdatePolicy()
}

class DismissSoftAppUpdateUseCase(
    private val repository: UpdatePolicyRepository,
) : DismissSoftAppUpdate {
    override suspend fun invoke(policyId: String, cooldownHours: Int) =
        repository.dismissSoftUpdate(policyId, cooldownHours)
}
