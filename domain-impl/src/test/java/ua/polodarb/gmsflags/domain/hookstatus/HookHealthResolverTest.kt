package ua.polodarb.gmsflags.domain.hookstatus

import org.junit.Assert.assertEquals
import org.junit.Test

class HookHealthResolverTest {
    @Test
    fun `requires restart when saved overrides differ from loaded session`() {
        assertEquals(
            HookHealth.RestartRequired,
            HookHealthResolver.resolve(record(current = 3, loaded = 2, consumed = 2)),
        )
    }

    @Test
    fun `reports working only after an override was consumed`() {
        assertEquals(
            HookHealth.Working,
            HookHealthResolver.resolve(record(current = 2, loaded = 2, consumed = 1)),
        )
    }

    @Test
    fun `installed hook without calls is ready rather than broken`() {
        assertEquals(
            HookHealth.HookReady,
            HookHealthResolver.resolve(record(current = 2, loaded = 2, consumed = 0)),
        )
    }

    @Test
    fun `saved overrides without a session are not checked`() {
        assertEquals(
            HookHealth.NotChecked,
            HookHealthResolver.resolve(HookDiagnosticRecord("target", 2)),
        )
    }

    private fun record(current: Int, loaded: Int, consumed: Int) = HookDiagnosticRecord(
        androidPackageName = "target",
        currentOverrideCount = current,
        session = HookSession(
            processName = "target",
            versionCode = 1,
            startedAt = 1,
            updatedAt = 2,
            loadedOverrideCount = loaded,
            state = HookSessionState.Installed,
            strategies = listOf(
                HookStrategyDiagnostic(
                    name = "strategy",
                    state = HookStrategyState.Installed,
                    appliedCount = consumed,
                    consumedCount = consumed,
                )
            ),
        ),
    )
}
