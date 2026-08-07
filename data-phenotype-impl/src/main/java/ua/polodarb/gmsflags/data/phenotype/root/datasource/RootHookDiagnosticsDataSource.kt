package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.phenotype.root.parcel.HookDiagnosticSnapshotParcel
import ua.polodarb.gmsflags.data.repository.hookstatus.datasource.HookDiagnosticsDataSource
import ua.polodarb.gmsflags.domain.hookstatus.HookDiagnosticRecord
import ua.polodarb.gmsflags.domain.hookstatus.HookCompatibilityWarning
import ua.polodarb.gmsflags.domain.hookstatus.HookSession
import ua.polodarb.gmsflags.domain.hookstatus.HookSessionState
import ua.polodarb.gmsflags.domain.hookstatus.HookStrategyDiagnostic
import ua.polodarb.gmsflags.domain.hookstatus.HookStrategyState
import ua.polodarb.xposed.info.HookDiagnosticContract

internal class RootHookDiagnosticsDataSource(
    private val connector: PhenotypeRootServiceConnector,
) : HookDiagnosticsDataSource {
    override suspend fun read(
        androidPackageNames: List<String>,
    ): Result<List<HookDiagnosticRecord>> = connector.call { service ->
        service.readHookDiagnostics(androidPackageNames).map(HookDiagnosticSnapshotParcel::toDomain)
    }
}

private fun HookDiagnosticSnapshotParcel.toDomain() = HookDiagnosticRecord(
    androidPackageName = androidPackageName,
    currentOverrideCount = currentOverrideCount,
    compatibilityWarnings = compatibilityWarnings.mapNotNullTo(mutableSetOf()) { warning ->
        when (warning) {
            HookDiagnosticContract.COMPATIBILITY_WARNING_PAIRIP_CORE ->
                HookCompatibilityWarning.PairipCore
            else -> null
        }
    },
    session = takeIf { hasSession }?.let {
        HookSession(
            processName = processName,
            versionCode = versionCode,
            startedAt = startedAt,
            updatedAt = updatedAt,
            loadedOverrideCount = loadedOverrideCount,
            state = when (state) {
                HookDiagnosticContract.STATE_INSTALLED -> HookSessionState.Installed
                HookDiagnosticContract.STATE_NO_OVERRIDES -> HookSessionState.NoOverrides
                HookDiagnosticContract.STATE_PAUSED -> HookSessionState.Paused
                HookDiagnosticContract.STATE_FAILED -> HookSessionState.Failed
                else -> HookSessionState.Started
            },
            error = error,
            strategies = strategies.map { strategy ->
                HookStrategyDiagnostic(
                    name = strategy.name,
                    state = when (strategy.state) {
                        HookDiagnosticContract.STRATEGY_STATE_INSTALLED -> HookStrategyState.Installed
                        HookDiagnosticContract.STRATEGY_STATE_UNAVAILABLE -> HookStrategyState.Unavailable
                        HookDiagnosticContract.STRATEGY_STATE_FAILED -> HookStrategyState.Failed
                        else -> HookStrategyState.Pending
                    },
                    appliedCount = strategy.appliedCount,
                    consumedCount = strategy.consumedCount,
                    message = strategy.message,
                )
            },
        )
    },
)
