package ua.polodarb.gmsflags.domain.hookstatus

object HookHealthResolver {
    fun resolve(record: HookDiagnosticRecord): HookHealth {
        val session = record.session ?: return if (record.currentOverrideCount == 0) {
            HookHealth.NoOverrides
        } else {
            HookHealth.NotChecked
        }
        if (session.state == HookSessionState.Failed) return HookHealth.Error
        if (session.state == HookSessionState.Paused) return HookHealth.Paused
        if (record.currentOverrideCount != session.loadedOverrideCount) {
            return HookHealth.RestartRequired
        }
        if (record.currentOverrideCount == 0 || session.state == HookSessionState.NoOverrides) {
            return HookHealth.NoOverrides
        }

        val installed = session.strategies.filter { it.state == HookStrategyState.Installed }
        if (installed.isEmpty()) return HookHealth.Error
        val applied = installed.sumOf(HookStrategyDiagnostic::appliedCount)
        val consumed = installed.sumOf(HookStrategyDiagnostic::consumedCount)
        return when {
            consumed > 0 -> HookHealth.Working
            applied > 0 -> HookHealth.Partial
            else -> HookHealth.HookReady
        }
    }
}
