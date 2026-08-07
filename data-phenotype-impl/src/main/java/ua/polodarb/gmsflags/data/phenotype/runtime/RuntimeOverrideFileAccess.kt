package ua.polodarb.gmsflags.data.phenotype.runtime

import android.content.pm.PackageManager
import android.system.Os
import java.io.File

internal interface RuntimeOverrideFileAccess {
    fun prepare(
        androidPackageName: String,
        databaseFile: File,
        restoreContext: Boolean,
    )
}

internal class AndroidRuntimeOverrideFileAccess(
    private val packageManager: PackageManager,
) : RuntimeOverrideFileAccess {
    override fun prepare(
        androidPackageName: String,
        databaseFile: File,
        restoreContext: Boolean,
    ) {
        val uid = packageManager.getApplicationInfo(androidPackageName, 0).uid
        databaseFile.parentFile?.let { directory ->
            Os.chown(directory.path, uid, uid)
            Os.chmod(directory.path, MODE_OWNER_DIRECTORY)
            Os.chown(databaseFile.path, uid, uid)
            Os.chmod(databaseFile.path, MODE_OWNER_FILE)
            if (restoreContext) restoreSecurityContext(directory, databaseFile)
        }
    }

    private fun restoreSecurityContext(directory: File, databaseFile: File) {
        val exitCode = ProcessBuilder("restorecon", "-F", directory.path, databaseFile.path)
            .redirectErrorStream(true)
            .start()
            .waitFor()
        check(exitCode == 0) {
            "Unable to restore SELinux context for runtime overrides"
        }
    }

    private companion object {
        const val MODE_OWNER_DIRECTORY = 448
        const val MODE_OWNER_FILE = 384
    }
}
