package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document

import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.FlagImportDocument

fun interface FlagImportDocumentSource {
    suspend fun read(documentUri: String): FlagImportDocument
}
