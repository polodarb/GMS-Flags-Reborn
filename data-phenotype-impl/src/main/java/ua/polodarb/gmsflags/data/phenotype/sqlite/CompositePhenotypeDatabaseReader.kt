package ua.polodarb.gmsflags.data.phenotype.sqlite

internal class CompositePhenotypeDatabaseReader(
    private val readers: List<PhenotypeDatabaseReader>,
) : PhenotypeDatabaseReader {
    init {
        require(readers.isNotEmpty()) { "At least one Phenotype database reader is required" }
    }

    override fun readPhenotypePackages(): List<PhenotypePackageBindingRecord> {
        val results = readers.map { reader -> runCatching(reader::readPhenotypePackages) }
        val successfulResults = results.mapNotNull { it.getOrNull() }

        if (successfulResults.isEmpty()) {
            throw results.firstNotNullOf { it.exceptionOrNull() }
        }

        return successfulResults
            .flatten()
            .distinctBy { it.androidPackageName to it.phenotypePackageName }
    }
}
