package ua.polodarb.gmsflags.data.repository.impl.server.mapper

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import ua.polodarb.gmsflags.data.network.publicapi.model.HookCompatibilityProfileNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.HookCompatibilityResolveNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ResolveResponseNetModel
import ua.polodarb.gmsflags.domain.server.sync.HookAdapter
import ua.polodarb.gmsflags.domain.server.sync.HookCompatibility
import ua.polodarb.gmsflags.domain.server.sync.HookCompatibilityProfile
import ua.polodarb.gmsflags.domain.server.sync.HookCompatibilityStatus
import ua.polodarb.gmsflags.domain.server.sync.HookSupportStatus
import ua.polodarb.gmsflags.domain.server.sync.RemoteFlagConfiguration
import ua.polodarb.gmsflags.domain.server.sync.RemoteResolvedFlag

internal fun ResolveResponseNetModel.toDomain() = RemoteFlagConfiguration(
    packageName = packageName,
    versionCode = versionCode,
    flags = flags.map { flag ->
        RemoteResolvedFlag(
            name = flag.flagName,
            type = flag.valueType.toRemoteFlagValueType(),
            value = when (val value = flag.value) {
                JsonNull -> "null"
                is JsonPrimitive -> value.content
                else -> value.toString()
            },
        )
    },
    configHash = configHash,
)

internal fun HookCompatibilityResolveNetModel.toDomain() = HookCompatibility(
    status = when (status.uppercase()) {
        "SUPPORTED" -> HookSupportStatus.Supported
        "UNSUPPORTED" -> HookSupportStatus.Unsupported
        else -> HookSupportStatus.Unknown
    },
    profile = profile?.toDomain(),
)

private fun HookCompatibilityProfileNetModel.toDomain() = HookCompatibilityProfile(
    id = id,
    adapter = adapter.toHookAdapter(),
    versionConstraint = versionConstraint.toDomain(),
    anchorGroups = anchorGroups,
    enabled = enabled,
    priority = priority,
    status = compatibilityStatus.toHookCompatibilityStatus(),
    notes = notes,
)

private fun String.toHookAdapter() = when (uppercase()) {
    "PHENOTYPE_RUNTIME_V1" -> HookAdapter.PhenotypeRuntimeV1
    "TYPED_REGISTRY_V1" -> HookAdapter.TypedRegistryV1
    "FINSKY_EXPERIMENTS_V1" -> HookAdapter.FinskyExperimentsV1
    "KEEP_FILE_V1" -> HookAdapter.KeepFileV1
    else -> HookAdapter.Unknown
}

private fun String.toHookCompatibilityStatus() = when (uppercase()) {
    "DISCOVERED" -> HookCompatibilityStatus.Discovered
    "OBSERVED" -> HookCompatibilityStatus.Observed
    "VERIFIED" -> HookCompatibilityStatus.Verified
    else -> HookCompatibilityStatus.Unknown
}
