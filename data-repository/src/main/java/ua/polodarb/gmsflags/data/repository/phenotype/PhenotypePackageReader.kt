package ua.polodarb.gmsflags.data.repository.phenotype

fun interface PhenotypePackageReader {
    suspend fun readPhenotypePackages(): Result<List<PhenotypePackageBinding>>
}

data class PhenotypePackageBinding(
    val androidPackageName: String,
    val phenotypePackageName: String,
)
