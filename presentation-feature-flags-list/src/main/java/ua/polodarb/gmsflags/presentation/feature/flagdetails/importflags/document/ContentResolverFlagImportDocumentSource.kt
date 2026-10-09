package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import ua.polodarb.gmsflags.presentation.core.document.readBoundedUtf8
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
        val content = contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBoundedUtf8(MAX_DOCUMENT_BYTES)
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

    private companion object {
        const val MAX_DOCUMENT_BYTES = 64 * 1024 * 1024
    }
}
