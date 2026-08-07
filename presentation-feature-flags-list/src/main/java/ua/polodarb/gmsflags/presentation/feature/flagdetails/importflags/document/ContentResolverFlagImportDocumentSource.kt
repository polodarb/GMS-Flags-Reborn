package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.Reader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.FlagImportDocument

internal class ContentResolverFlagImportDocumentSource(
    private val contentResolver: ContentResolver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FlagImportDocumentSource {
    override suspend fun read(documentUri: String): FlagImportDocument = withContext(ioDispatcher) {
        val uri = Uri.parse(documentUri)
        val displayName = contentResolver.displayNameOrNull(uri)
        val content = contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            reader.readLimited()
        }
            ?: error("Unable to open import document")

        FlagImportDocument(
            displayName = displayName ?: uri.lastPathSegment
                ?.substringAfterLast('/')
                .orEmpty(),
            content = content,
        )
    }

    private fun ContentResolver.displayNameOrNull(uri: Uri): String? = runCatching {
        query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameColumn < 0) null else cursor.getString(nameColumn)
        }
    }.getOrNull()

    private fun Reader.readLimited(): String {
        val result = StringBuilder()
        val buffer = CharArray(READ_BUFFER_SIZE)
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            require(result.length + count <= MAX_DOCUMENT_CHARS) {
                "Import document is too large"
            }
            result.append(buffer, 0, count)
        }
        return result.toString()
    }

    private companion object {
        const val READ_BUFFER_SIZE = 8 * 1024
        const val MAX_DOCUMENT_CHARS = 4 * 1024 * 1024
    }
}
