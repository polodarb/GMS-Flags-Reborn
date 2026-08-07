package ua.polodarb.gmsflags.data.phenotype.sqlite

fun interface PhenotypeDatabaseReader {
    fun readPhenotypePackages(): List<PhenotypePackageBindingRecord>
}

data class PhenotypePackageBindingRecord(
    val phenotypePackageName: String,
    val androidPackageName: String,
)
