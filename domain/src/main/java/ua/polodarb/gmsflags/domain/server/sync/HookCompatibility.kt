package ua.polodarb.gmsflags.domain.server.sync

import ua.polodarb.gmsflags.domain.server.content.VersionConstraint

data class HookCompatibility(
    val status: HookSupportStatus,
    val profile: HookCompatibilityProfile?,
) {
    val supported: Boolean
        get() = status == HookSupportStatus.Supported && profile != null
}

enum class HookSupportStatus { Supported, Unsupported, Unknown }

data class HookCompatibilityProfile(
    val id: Long,
    val adapter: HookAdapter,
    val versionConstraint: VersionConstraint,
    val anchorGroups: List<List<String>>,
    val enabled: Boolean,
    val priority: Int,
    val status: HookCompatibilityStatus,
    val notes: String?,
)

enum class HookAdapter {
    PhenotypeRuntimeV1,
    TypedRegistryV1,
    FinskyExperimentsV1,
    KeepFileV1,
    Unknown,
}

enum class HookCompatibilityStatus { Discovered, Observed, Verified, Unknown }
