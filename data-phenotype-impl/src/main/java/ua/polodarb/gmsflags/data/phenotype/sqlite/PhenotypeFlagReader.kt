package ua.polodarb.gmsflags.data.phenotype.sqlite

fun interface PhenotypeFlagReader {
    fun readFlags(
        androidPackageName: String,
        phenotypePackageName: String,
    ): List<StoredPhenotypeFlag>
}
