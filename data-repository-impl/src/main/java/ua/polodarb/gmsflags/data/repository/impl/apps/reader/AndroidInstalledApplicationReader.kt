package ua.polodarb.gmsflags.data.repository.impl.apps.reader

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidInstalledApplicationReader(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : InstalledApplicationReader {
    private val packageManager = context.applicationContext.packageManager

    override suspend fun readInstalledApplications(
        packageNames: Set<String>,
    ): Map<String, InstalledApplicationMetadata> = withContext(dispatcher) {
        if (packageNames.isEmpty()) return@withContext emptyMap()

        installedPackages().asSequence()
            .filter { it.packageName in packageNames }
            .mapNotNull { packageInfo ->
                val applicationInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                if (packageInfo.versionName.isStubVersion()) return@mapNotNull null
                val packageName = packageInfo.packageName
                packageName to InstalledApplicationMetadata(
                    packageName = packageName,
                    name = applicationInfo.loadLabel(packageManager).toString().ifBlank { packageName },
                    versionName = packageInfo.versionName,
                    versionCode = packageInfo.longVersionCode,
                    lastUpdateTime = packageInfo.lastUpdateTime,
                )
            }
            .toMap()
    }

    private fun String?.isStubVersion(): Boolean = this?.contains(STUB_VERSION_MARKER, ignoreCase = true) == true

    private fun installedPackages() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getInstalledPackages(0)
    }

    private companion object {
        const val STUB_VERSION_MARKER = "-stub"
    }
}
