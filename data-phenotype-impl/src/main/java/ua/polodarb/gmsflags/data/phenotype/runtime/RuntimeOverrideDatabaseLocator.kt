package ua.polodarb.gmsflags.data.phenotype.runtime

import android.content.pm.PackageManager
import java.io.File
import ua.polodarb.xposed.info.XposedConstants

internal fun interface RuntimeOverrideDatabaseLocator {
    fun locate(androidPackageName: String): File
}

internal class AndroidRuntimeOverrideDatabaseLocator(
    private val packageManager: PackageManager,
) : RuntimeOverrideDatabaseLocator {
    override fun locate(androidPackageName: String): File {
        val dataDirectory = packageManager.getApplicationInfo(androidPackageName, 0).dataDir
        return File(
            File(dataDirectory, XposedConstants.XPOSED_DIR),
            XposedConstants.RUNTIME_OVERRIDES_DB_FILE_NAME,
        )
    }
}
