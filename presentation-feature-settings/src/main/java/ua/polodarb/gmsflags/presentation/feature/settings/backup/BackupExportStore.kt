package ua.polodarb.gmsflags.presentation.feature.settings.backup

import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.presentation.core.document.readBoundedUtf8

internal interface BackupExportStore {
    suspend fun prepare(xml: String): String

    suspend fun read(id: String): String

    suspend fun delete(id: String)
}

internal class FileBackupExportStore(private val directory: File) : BackupExportStore {
    override suspend fun prepare(xml: String): String = withContext(Dispatchers.IO) {
        check(directory.isDirectory || directory.mkdirs()) {
            "Unable to prepare backup directory"
        }
        val id = UUID.randomUUID().toString()
        val file = file(id)
        try {
            FileOutputStream(file).use { output ->
                output.write(xml.toByteArray(Charsets.UTF_8))
                output.fd.sync()
            }
            id
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }

    override suspend fun read(id: String): String = withContext(Dispatchers.IO) {
        file(id).inputStream().use { it.readBoundedUtf8(64 * 1024 * 1024) }
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val file = file(id)
        check(!file.exists() || file.delete()) { "Unable to remove prepared backup" }
    }

    private fun file(id: String): File {
        require(UUID.fromString(id).toString() == id) { "Invalid export identifier" }
        return File(directory, "$id.gmsbackup")
    }
}
