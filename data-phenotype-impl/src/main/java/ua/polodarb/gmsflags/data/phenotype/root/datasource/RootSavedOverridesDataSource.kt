package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeOverrideRecord
import ua.polodarb.gmsflags.data.repository.phenotype.SavedOverrideRecord
import ua.polodarb.gmsflags.data.repository.phenotype.SavedOverridesDataSource

internal class RootSavedOverridesDataSource(
    private val connector: PhenotypeRootServiceConnector,
) : SavedOverridesDataSource {
    override suspend fun readAll(androidPackageName: String): Result<List<SavedOverrideRecord>> =
        connector.call { service ->
            buildList {
                var offset = 0
                while (true) {
                    val page = service.readSavedOverridesPage(androidPackageName, offset)
                    if (page.isEmpty()) {
                        break
                    }
                    page.forEach {
                        add(
                            SavedOverrideRecord(
                                it.packageName,
                                PhenotypeOverrideRecord(it.flag.name, it.flag.type, it.flag.value),
                            )
                        )
                    }
                    offset += page.size
                }
            }
        }
}
