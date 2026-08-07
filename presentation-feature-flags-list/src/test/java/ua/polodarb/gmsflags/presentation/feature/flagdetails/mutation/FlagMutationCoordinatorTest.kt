package ua.polodarb.gmsflags.presentation.feature.flagdetails.mutation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FlagMutationCoordinatorTest {
    @Test
    fun `only latest generation completes the mutation sequence`() = runBlocking {
        val coordinator = FlagMutationCoordinator()
        val first = coordinator.begin(resetFailure = true)
        val latest = coordinator.begin(resetFailure = false)

        assertEquals(
            FlagMutationResult.AwaitingLatest,
            coordinator.execute(first) { Result.success(Unit) },
        )
        assertEquals(
            FlagMutationResult.Success,
            coordinator.execute(latest) { Result.success(Unit) },
        )
    }

    @Test
    fun `earlier failure is reported when latest generation completes`() = runBlocking {
        val coordinator = FlagMutationCoordinator()
        val failure = IllegalStateException("write failed")
        val first = coordinator.begin(resetFailure = true)
        val latest = coordinator.begin(resetFailure = false)

        coordinator.execute(first) { Result.failure(failure) }
        val result = coordinator.execute(latest) { Result.success(Unit) }

        assertSame(failure, (result as FlagMutationResult.Failure).error)
    }
}
