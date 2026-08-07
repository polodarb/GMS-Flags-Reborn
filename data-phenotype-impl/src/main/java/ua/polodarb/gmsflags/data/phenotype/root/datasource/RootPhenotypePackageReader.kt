package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypePackageReader
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypePackageBinding

internal class RootPhenotypePackageReader(
    private val connector: PhenotypeRootServiceConnector,
) : PhenotypePackageReader {
    override suspend fun readPhenotypePackages(): Result<List<PhenotypePackageBinding>> {
        return connector.call { service ->
            service.readPhenotypePackages().mapNotNull { binding ->
                val phenotypePackage = binding.phenotypePackageName
                    .takeIf(String::isNotBlank) ?: return@mapNotNull null
                val androidPackage = binding.androidPackageName
                    .takeIf(String::isNotBlank) ?: return@mapNotNull null
                PhenotypePackageBinding(
                    androidPackageName = androidPackage,
                    phenotypePackageName = phenotypePackage,
                )
            }
        }
    }
}
