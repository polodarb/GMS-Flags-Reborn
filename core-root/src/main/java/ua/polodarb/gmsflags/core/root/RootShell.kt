package ua.polodarb.gmsflags.core.root

interface RootShell {
    fun isRoot(): Boolean
    fun execute(command: String): RootCommandResult
}
