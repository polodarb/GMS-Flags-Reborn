package ua.polodarb.gmsflags.data.repository.update

import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy

interface UpdatePolicyRepository {
    suspend fun getUpdatePolicy(): AppUpdatePolicy?

    /** Snoozes a soft update for [cooldownHours] so it stops nagging until then. */
    suspend fun dismissSoftUpdate(policyId: String, cooldownHours: Int)
}
