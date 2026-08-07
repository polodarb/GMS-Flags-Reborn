package ua.polodarb.xposed.runtime

import android.content.Context
import java.io.File
import ua.polodarb.xposed.info.XposedConstants

internal class XposedRuntimeDirectory {
    fun resolve(context: Context): File = candidates(context)
        .firstOrNull(::isWritable)
        ?: File(context.dataDir, XposedConstants.XPOSED_DIR)

    private fun candidates(context: Context): List<File> = listOf(
        File(context.dataDir, XposedConstants.XPOSED_DIR),
        File(
            context.dataDir.path.replace("/user/", "/user_de/"),
            XposedConstants.XPOSED_DIR,
        ),
    ).distinctBy(File::getAbsolutePath)

    private fun isWritable(directory: File): Boolean = runCatching {
        (directory.exists() || directory.mkdirs()) && directory.canWrite()
    }.getOrDefault(false)
}
