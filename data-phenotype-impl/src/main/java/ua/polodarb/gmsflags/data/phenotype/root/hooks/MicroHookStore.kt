package ua.polodarb.gmsflags.data.phenotype.root.hooks

import ua.polodarb.gmsflags.data.phenotype.root.parcel.MicroHookEnvelopeParcel
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeFlagOverrideStore
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeMicroHookOverride

internal class MicroHookStore(
    private val overrideStore: RuntimeFlagOverrideStore,
) {
    fun writeMicroHooks(androidPackageName: String, hooks: List<MicroHookEnvelopeParcel>) {
        overrideStore.writeMicroHooks(
            androidPackageName,
            hooks.map { hook ->
                RuntimeMicroHookOverride(
                    recipeId = hook.recipeId,
                    payloadBase64 = hook.payloadBase64,
                    payloadSha256 = hook.payloadSha256,
                    signatureBase64 = hook.signatureBase64,
                    required = hook.required,
                )
            },
        )
    }

    fun deleteMicroHooks(androidPackageName: String, recipeIds: List<Long>) {
        overrideStore.deleteMicroHooks(androidPackageName, recipeIds)
    }
}
