package ua.polodarb.gmsflags.data.phenotype.root.connector

import android.content.Context
import ua.polodarb.gmsflags.data.phenotype.root.diagnostic.LsposedScopeReader
import ua.polodarb.gmsflags.data.phenotype.root.diagnostic.XposedLogsReader
import ua.polodarb.gmsflags.data.phenotype.root.flags.OverrideRuntimeController
import ua.polodarb.gmsflags.data.phenotype.root.flags.PhenotypeFlagStore
import ua.polodarb.gmsflags.data.phenotype.root.hooks.HookDiagnosticsReader
import ua.polodarb.gmsflags.data.phenotype.root.hooks.MicroHookStore
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeFlagOverrideStore
import ua.polodarb.gmsflags.data.phenotype.sqlite.CompositePhenotypeDatabaseReader
import ua.polodarb.gmsflags.data.phenotype.sqlite.PhenotypeDatabasePaths
import ua.polodarb.gmsflags.data.phenotype.sqlite.RoutedPhenotypeFlagReader
import ua.polodarb.gmsflags.data.phenotype.sqlite.SqlitePhenotypeDatabaseReader
import ua.polodarb.gmsflags.data.phenotype.sqlite.SqlitePhenotypeFlagDatabaseReader
import ua.polodarb.xposed.info.XposedTargets

internal class PhenotypeRootOperationsFactory(
    private val context: Context,
) {
    fun create(): PhenotypeRootOperations {
        val gmsPackageReader = SqlitePhenotypeDatabaseReader(PhenotypeDatabasePaths.GMS)
        val vendingPackageReader = SqlitePhenotypeDatabaseReader(PhenotypeDatabasePaths.VENDING)
        val gmsFlagReader = SqlitePhenotypeFlagDatabaseReader(PhenotypeDatabasePaths.GMS)
        val vendingFlagReader = SqlitePhenotypeFlagDatabaseReader(PhenotypeDatabasePaths.VENDING)
        val runtimeOverrideStore = RuntimeFlagOverrideStore(context)

        return DefaultPhenotypeRootOperations(
            packageReader = CompositePhenotypeDatabaseReader(
                readers = listOf(gmsPackageReader, vendingPackageReader),
            ),
            flagStore = PhenotypeFlagStore(
                flagReader = RoutedPhenotypeFlagReader(
                    defaultReaders = listOf(gmsFlagReader),
                    readersByApplication = mapOf(
                        XposedTargets.VENDING_PACKAGE_NAME to listOf(
                            vendingFlagReader,
                            gmsFlagReader,
                        ),
                    ),
                ),
                overrideStore = runtimeOverrideStore,
            ),
            microHookStore = MicroHookStore(overrideStore = runtimeOverrideStore),
            hookDiagnosticsReader = HookDiagnosticsReader(context.packageManager),
            xposedScopeReader = LsposedScopeReader(),
            xposedLogsReader = XposedLogsReader(context.packageManager),
            overrideRuntimeController = OverrideRuntimeController(
                packageManager = context.packageManager,
                overrideStore = runtimeOverrideStore,
            ),
        )
    }
}
