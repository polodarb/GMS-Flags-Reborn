package ua.polodarb.gmsflags.domain.hookstatus

data class HookDiagnosticRecord(
    val androidPackageName: String,
    val currentOverrideCount: Int,
    val session: HookSession? = null,
    val compatibilityWarnings: Set<HookCompatibilityWarning> = emptySet(),
)

enum class HookCompatibilityWarning { PairipCore }

data class HookSession(
    val processName: String,
    val versionCode: Long,
    val startedAt: Long,
    val updatedAt: Long,
    val loadedOverrideCount: Int,
    val state: HookSessionState,
    val error: String? = null,
    val strategies: List<HookStrategyDiagnostic> = emptyList(),
)

data class HookStrategyDiagnostic(
    val name: String,
    val state: HookStrategyState,
    val appliedCount: Int,
    val consumedCount: Int,
    val message: String? = null,
)

enum class HookSessionState { Started, Installed, NoOverrides, Paused, Failed }
enum class HookStrategyState { Pending, Installed, Unavailable, Failed }

enum class HookHealth {
    NotChecked,
    NoOverrides,
    Paused,
    RestartRequired,
    HookReady,
    Working,
    Partial,
    Error,
}

data class HookApplicationStatus(
    val androidPackageName: String,
    val applicationName: String,
    val versionName: String?,
    val health: HookHealth,
    val currentOverrideCount: Int,
    val loadedOverrideCount: Int,
    val installedStrategyCount: Int,
    val appliedCount: Int,
    val consumedCount: Int,
    val lastCheckedAt: Long?,
    val session: HookSession?,
    val compatibilityWarnings: Set<HookCompatibilityWarning> = emptySet(),
)

data class HookStatusOverview(
    val applications: List<HookApplicationStatus>,
) {
    val moduleObserved: Boolean = applications.any { it.session != null }
    val workingApplicationCount: Int = applications.count {
        it.health == HookHealth.Working && it.compatibilityWarnings.isEmpty()
    }
    val attentionApplicationCount: Int = applications.count {
        it.compatibilityWarnings.isNotEmpty() ||
            it.health == HookHealth.Error ||
            it.health == HookHealth.Partial ||
            it.health == HookHealth.RestartRequired
    }
}
