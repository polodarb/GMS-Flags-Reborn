package ua.polodarb.gmsflags.presentation.feature.flagdetails.mutation

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.yield
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType

class FlagOverrideWriteQueueTest {
    @Test
    fun rapidChangesAreCollapsedToLatestValues() = runBlocking {
        val writes = mutableListOf<List<FlagOverride>>()
        val failures = mutableListOf<Throwable>()
        val queue = FlagOverrideWriteQueue(
            scope = this,
            writer = { batch -> writes += batch; Result.success(Unit) },
            onFailure = failures::add,
            debounceDelayMillis = 0L,
        )

        queue.submit(listOf(booleanOverride("first", true)))
        queue.submit(listOf(booleanOverride("first", false)))
        queue.submit(listOf(booleanOverride("second", true)))
        queue.awaitIdle()

        assertEquals(1, writes.size)
        assertEquals(
            mapOf("first" to "false", "second" to "true"),
            writes.single().associate { it.name to it.value },
        )
        assertTrue(failures.isEmpty())
    }

    @Test
    fun changesDuringWriteProduceOneLatestFollowUpBatch() = runBlocking {
        val firstWriteStarted = CompletableDeferred<Unit>()
        val releaseFirstWrite = CompletableDeferred<Unit>()
        val writes = mutableListOf<List<FlagOverride>>()
        val queue = FlagOverrideWriteQueue(
            scope = this,
            writer = { batch ->
                writes += batch
                if (writes.size == 1) {
                    firstWriteStarted.complete(Unit)
                    releaseFirstWrite.await()
                }
                Result.success(Unit)
            },
            onFailure = { throw it },
            debounceDelayMillis = 0L,
        )

        queue.submit(listOf(booleanOverride("flag", true)))
        firstWriteStarted.await()
        queue.submit(listOf(booleanOverride("flag", false)))
        queue.submit(listOf(booleanOverride("flag", true)))
        queue.submit(listOf(booleanOverride("flag", false)))
        yield()
        releaseFirstWrite.complete(Unit)
        queue.awaitIdle()

        assertEquals(2, writes.size)
        assertEquals("false", writes.last().single().value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun newChangesRestartDebounceWindow() = runTest {
        val writes = mutableListOf<List<FlagOverride>>()
        val queue = FlagOverrideWriteQueue(
            scope = this,
            writer = { batch -> writes += batch; Result.success(Unit) },
            onFailure = { throw it },
            debounceDelayMillis = 600L,
        )

        queue.submit(listOf(booleanOverride("first", true)))
        runCurrent()
        advanceTimeBy(400L)
        queue.submit(listOf(booleanOverride("second", true)))
        runCurrent()
        advanceTimeBy(599L)
        runCurrent()

        assertTrue(writes.isEmpty())

        advanceTimeBy(1L)
        runCurrent()
        assertEquals(1, writes.size)
    }

    private fun booleanOverride(name: String, enabled: Boolean) = FlagOverride(
        name = name,
        type = FlagType.Boolean,
        value = enabled.toString(),
    )
}
