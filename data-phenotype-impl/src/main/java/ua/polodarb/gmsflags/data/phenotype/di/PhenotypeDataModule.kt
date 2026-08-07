package ua.polodarb.gmsflags.data.phenotype.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.phenotype.root.datasource.RootPhenotypeFlagsDataSource
import ua.polodarb.gmsflags.data.phenotype.root.datasource.RootPhenotypeHooksDataSource
import ua.polodarb.gmsflags.data.phenotype.root.datasource.RootPhenotypePackageReader
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagsDataSource
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeHooksDataSource
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypePackageReader
import ua.polodarb.gmsflags.data.repository.hookstatus.datasource.HookDiagnosticsDataSource
import ua.polodarb.gmsflags.data.phenotype.root.datasource.RootHookDiagnosticsDataSource
import ua.polodarb.gmsflags.data.repository.report.datasource.XposedLogsDataSource
import ua.polodarb.gmsflags.data.phenotype.root.datasource.RootXposedLogsDataSource
import ua.polodarb.gmsflags.data.phenotype.root.datasource.RootXposedScopeDataSource
import ua.polodarb.gmsflags.data.repository.apps.datasource.XposedScopeDataSource
import ua.polodarb.gmsflags.data.repository.settings.datasource.OverrideControlDataSource
import ua.polodarb.gmsflags.data.phenotype.root.datasource.RootOverrideControlDataSource

val phenotypeDataModule = module {
    single { PhenotypeRootServiceConnector(context = get(), rootAccessManager = get()) }
    single(named("scopeConnector")) {
        PhenotypeRootServiceConnector(context = get(), rootAccessManager = get())
    }
    single<PhenotypePackageReader> {
        RootPhenotypePackageReader(connector = get())
    }
    single<PhenotypeFlagsDataSource> { RootPhenotypeFlagsDataSource(connector = get()) }
    single<PhenotypeHooksDataSource> { RootPhenotypeHooksDataSource(connector = get()) }
    single<HookDiagnosticsDataSource> { RootHookDiagnosticsDataSource(connector = get()) }
    single<XposedScopeDataSource> {
        RootXposedScopeDataSource(connector = get(named("scopeConnector")))
    }
    single<OverrideControlDataSource> { RootOverrideControlDataSource(connector = get()) }
    single<XposedLogsDataSource> { RootXposedLogsDataSource(connector = get()) }
}
