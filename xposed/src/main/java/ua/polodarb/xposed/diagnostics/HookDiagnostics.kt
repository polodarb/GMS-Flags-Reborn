package ua.polodarb.xposed.diagnostics

internal interface HookDiagnostics {
    fun start(overrideCount: Int)
    fun noOverrides()
    fun paused()
    fun strategyInstalled(strategy: String)
    fun strategyUnavailable(strategy: String, reason: String)
    fun strategyFailed(strategy: String, reason: String)
    fun overrideApplied(strategy: String, identity: String)
    fun overrideConsumed(strategy: String, identity: String)

    fun overrideAppliedAndConsumed(strategy: String, identity: String) {
        overrideApplied(strategy, identity)
        overrideConsumed(strategy, identity)
    }

    fun failure(message: String)

    companion object {
        val None: HookDiagnostics = object : HookDiagnostics {
            override fun start(overrideCount: Int) = Unit
            override fun noOverrides() = Unit
            override fun paused() = Unit
            override fun strategyInstalled(strategy: String) = Unit
            override fun strategyUnavailable(strategy: String, reason: String) = Unit
            override fun strategyFailed(strategy: String, reason: String) = Unit
            override fun overrideApplied(strategy: String, identity: String) = Unit
            override fun overrideConsumed(strategy: String, identity: String) = Unit
            override fun failure(message: String) = Unit
        }
    }
}
