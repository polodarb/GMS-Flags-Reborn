package ua.polodarb.xposed.runtime

import android.annotation.SuppressLint
import java.io.File
import ua.polodarb.xposed.logging.XposedLogger

internal object ModuleNativeLibraryLoader {
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    fun load(moduleApkPath: String, libraryName: String) {
        val prefixedName = if (libraryName.startsWith("lib")) libraryName else "lib$libraryName"
        val fileName = if (prefixedName.endsWith(".so")) prefixedName else "$prefixedName.so"
        val nativeLibraryRoot = File(File(moduleApkPath).parentFile, "lib")
        val installedLibrary = nativeLibraryRoot.listFiles()
            ?.asSequence()
            ?.filter(File::isDirectory)
            ?.map { directory -> File(directory, fileName) }
            ?.firstOrNull(File::isFile)
            ?: throw UnsatisfiedLinkError("Native library $libraryName not found next to $moduleApkPath")

        System.load(installedLibrary.absolutePath)
        XposedLogger.logI("Loaded native library: ${installedLibrary.path}")
    }
}
