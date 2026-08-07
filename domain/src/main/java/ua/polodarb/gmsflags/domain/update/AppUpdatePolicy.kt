package ua.polodarb.gmsflags.domain.update

/** Force/soft update policy driven by Firebase Remote Config. Null when no update is required. */
data class AppUpdatePolicy(
    val policyId: String,
    val title: String,
    val description: String,
    val updateUrl: String,
    val type: AppUpdateType,
    val softCooldownHours: Int,
    val primaryButtonTitle: String,
    val secondaryButtonTitle: String,
)

enum class AppUpdateType { SOFT, FORCE }

fun interface GetAppUpdatePolicy {
    suspend operator fun invoke(): AppUpdatePolicy?
}

fun interface DismissSoftAppUpdate {
    suspend operator fun invoke(policyId: String, cooldownHours: Int)
}
