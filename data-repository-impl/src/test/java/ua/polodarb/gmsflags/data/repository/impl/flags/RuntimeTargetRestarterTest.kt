package ua.polodarb.gmsflags.data.repository.impl.flags

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.core.root.RootCommandExecutor
import ua.polodarb.gmsflags.core.root.RootCommandResult

class RootTargetProcessRestarterTest {
    @Test
    fun `stops a validated package exactly once`() = runBlocking {
        val executor = RecordingExecutor()

        val result = RootTargetProcessRestarter(executor).restart("com.google.android.apps.test")

        assertTrue(result.isSuccess)
        assertEquals(listOf("am force-stop com.google.android.apps.test"), executor.commands)
    }

    @Test
    fun `rejects shell input before execution`() = runBlocking {
        val executor = RecordingExecutor()

        val result = RootTargetProcessRestarter(executor).restart("com.google.test;reboot")

        assertTrue(result.isFailure)
        assertTrue(executor.commands.isEmpty())
    }
}

private class RecordingExecutor : RootCommandExecutor {
    val commands = mutableListOf<String>()

    override suspend fun execute(command: String): Result<RootCommandResult> {
        commands += command
        return Result.success(RootCommandResult(code = 0, output = emptyList(), errors = emptyList()))
    }
}
