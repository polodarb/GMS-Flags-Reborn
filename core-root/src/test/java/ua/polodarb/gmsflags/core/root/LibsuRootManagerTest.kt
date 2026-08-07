package ua.polodarb.gmsflags.core.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibsuRootManagerTest {
    @Test
    fun `executes validated command through injected shell`() = runBlocking {
        val shell = FakeRootShell(root = true)
        val manager = LibsuRootManager(shell, Dispatchers.Unconfined)

        val result = manager.execute("id").getOrThrow()

        assertEquals(0, result.code)
        assertEquals(listOf("id"), shell.commands)
    }

    @Test
    fun `does not execute command without root access`() = runBlocking {
        val shell = FakeRootShell(root = false)
        val manager = LibsuRootManager(shell, Dispatchers.Unconfined)

        val failure = manager.execute("id").exceptionOrNull()

        assertTrue(failure is RootAccessUnavailableException)
        assertTrue(shell.commands.isEmpty())
    }

    private class FakeRootShell(private val root: Boolean) : RootShell {
        val commands = mutableListOf<String>()

        override fun isRoot(): Boolean = root

        override fun execute(command: String): RootCommandResult {
            commands += command
            return RootCommandResult(0, emptyList(), emptyList())
        }
    }
}
