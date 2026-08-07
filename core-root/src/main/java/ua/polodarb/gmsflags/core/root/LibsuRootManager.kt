package ua.polodarb.gmsflags.core.root

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibsuRootManager(
    private val shell: RootShell,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RootAccessManager, RootCommandExecutor {

    override suspend fun requestAccess(): Result<Unit> = withContext(dispatcher) {
        runCatching {
            if (!shell.isRoot()) throw RootAccessUnavailableException()
        }
    }

    override suspend fun execute(command: String): Result<RootCommandResult> = withContext(dispatcher) {
        runCatching {
            require(command.isNotBlank()) { "Root command cannot be blank" }
            if (!shell.isRoot()) throw RootAccessUnavailableException()
            shell.execute(command)
        }
    }
}
