package ua.polodarb.gmsflags.data.phenotype.sqlite

internal class RoutedPhenotypeFlagReader(
    private val defaultReaders: List<PhenotypeFlagDatabaseReader>,
    private val readersByApplication: Map<String, List<PhenotypeFlagDatabaseReader>>,
) : PhenotypeFlagReader {
    init {
        require(defaultReaders.isNotEmpty()) { "At least one default flag reader is required" }
        require(readersByApplication.values.all { it.isNotEmpty() }) {
            "Application-specific flag reader chains cannot be empty"
        }
    }

    override fun readFlags(
        androidPackageName: String,
        phenotypePackageName: String,
    ): List<StoredPhenotypeFlag> {
        val readers = readersByApplication[androidPackageName] ?: defaultReaders
        var firstFailure: Throwable? = null
        var hasSuccessfulRead = false

        readers.forEach { reader ->
            val result = runCatching { reader.readFlags(phenotypePackageName) }
                .onSuccess { hasSuccessfulRead = true }
                .onFailure { error -> if (firstFailure == null) firstFailure = error }
                .getOrNull()

            if (!result.isNullOrEmpty()) return result
        }

        if (!hasSuccessfulRead) {
            throw requireNotNull(firstFailure)
        }

        return emptyList()
    }
}
