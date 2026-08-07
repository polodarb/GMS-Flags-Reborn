package ua.polodarb.gmsflags.core.root

import com.topjohnwu.superuser.Shell

class LibsuRootShell : RootShell {
    init {
        Shell.setDefaultBuilder(
            Shell.Builder.create().setFlags(
                Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER
            )
        )
    }

    override fun isRoot(): Boolean {
        Shell.getCachedShell()?.let { cached ->
            if (!cached.isRoot) runCatching { cached.close() }
        }
        return Shell.getShell().isRoot
    }

    override fun execute(command: String): RootCommandResult {
        val result = Shell.cmd(command).exec()
        return RootCommandResult(
            code = result.code,
            output = result.out,
            errors = result.err,
        )
    }
}
