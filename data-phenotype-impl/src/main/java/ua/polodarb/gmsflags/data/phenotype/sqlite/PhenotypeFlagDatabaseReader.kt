package ua.polodarb.gmsflags.data.phenotype.sqlite

data class StoredPhenotypeFlag(
    val name: String,
    val type: Int,
    val value: String,
)

fun interface PhenotypeFlagDatabaseReader {
    fun readFlags(phenotypePackageName: String): List<StoredPhenotypeFlag>
}
