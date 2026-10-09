package ua.polodarb.gmsflags.presentation.feature.settings.backup

import android.content.ContentResolver
import android.provider.OpenableColumns
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.presentation.core.document.readBoundedUtf8

internal interface BackupDocumentStore {
    suspend fun read(uri: String): String

    suspend fun displayName(uri: String): String? = null

    suspend fun write(uri: String, xml: String)
}

internal class BackupDocuments(private val resolver: ContentResolver) : BackupDocumentStore {
    override suspend fun read(uri: String): String = withContext(Dispatchers.IO) {
        val stream = resolver.openInputStream(uri.toUri()) ?: error("Unable to open backup")
        stream.use { it.readBoundedUtf8(MAX_BYTES) }
    }

    override suspend fun write(uri: String, xml: String) = withContext(Dispatchers.IO) {
        val stream = resolver.openOutputStream(uri.toUri(), "wt") ?: error("Unable to save backup")
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(xml) }
    }

    override suspend fun displayName(uri: String): String? = withContext(Dispatchers.IO) {
        resolver
            .query(uri.toUri(), arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
    }

    private companion object {
        const val MAX_BYTES = 64 * 1024 * 1024
    }
}
