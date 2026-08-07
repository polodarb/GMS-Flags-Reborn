package ua.polodarb.gmsflags.core.root

data class RootCommandResult(
    val code: Int,
    val output: List<String>,
    val errors: List<String>,
)

interface RootCommandExecutor {
    suspend fun execute(command: String): Result<RootCommandResult>
}
