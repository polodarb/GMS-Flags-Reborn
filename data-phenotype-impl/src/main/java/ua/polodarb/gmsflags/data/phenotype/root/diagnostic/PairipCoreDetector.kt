package ua.polodarb.gmsflags.data.phenotype.root.diagnostic

import android.content.pm.ApplicationInfo
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipFile

/** Detects PairIP without extracting or reading native library contents. */
internal class PairipCoreDetector {
    private val embeddedApkCache = ConcurrentHashMap<ApkFingerprint, Boolean>()

    fun isPresent(applicationInfo: ApplicationInfo): Boolean {
        val apkFiles = buildList {
            applicationInfo.sourceDir?.let { add(File(it)) }
            applicationInfo.splitSourceDirs.orEmpty().mapTo(this, ::File)
        }
        return isPresent(
            apkFiles = apkFiles,
            nativeLibraryDirectory = applicationInfo.nativeLibraryDir?.let(::File),
            nativeLibrariesExtracted =
                applicationInfo.flags and ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS != 0,
        )
    }

    internal fun isPresent(
        apkFiles: List<File>,
        nativeLibraryDirectory: File?,
        nativeLibrariesExtracted: Boolean,
    ): Boolean {
        if (nativeLibraryDirectory?.resolve(LIBRARY_NAME)?.isFile == true) return true
        if (nativeLibrariesExtracted) return false

        return apkFiles.distinct().any { apkFile ->
            val fingerprint = ApkFingerprint(
                path = apkFile.path,
                size = apkFile.length(),
                lastModified = apkFile.lastModified(),
            )
            embeddedApkCache.computeIfAbsent(fingerprint) {
                containsEmbeddedLibrary(apkFile)
            }
        }
    }

    private fun containsEmbeddedLibrary(apkFile: File): Boolean = runCatching {
        ZipFile(apkFile).use { archive ->
            archive.entries().asSequence().any { entry ->
                !entry.isDirectory &&
                    entry.name.startsWith("lib/") &&
                    entry.name.substringAfterLast('/') == LIBRARY_NAME
            }
        }
    }.getOrDefault(false)

    private data class ApkFingerprint(
        val path: String,
        val size: Long,
        val lastModified: Long,
    )

    private companion object {
        const val LIBRARY_NAME = "libpairipcore.so"
    }
}
