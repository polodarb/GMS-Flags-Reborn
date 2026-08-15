package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.gmsflags.domain.server.content.VerifyHookTrust
import ua.polodarb.xposed.info.needle.NeedleSignature
import java.util.Base64

/**
 * Verifies a hook's signed envelope against a trusted public key that is injected in (never read
 * from BuildConfig directly - domain/domain-impl must not depend on the app module). [trustedPublicKeyBase64]
 * is null/blank whenever this build has no local key to check against (e.g. a release build without
 * the debug-only key baked in); that is a normal, expected "can't check" state, not a failure.
 */
class VerifyHookTrustUseCase(
    private val trustedPublicKeyBase64: String?,
) : VerifyHookTrust {
    override fun invoke(hook: RecommendationVariantHook): HookTrustStatus {
        val envelope = hook.envelope ?: return HookTrustStatus.NOT_SIGNED
        val trustedKey = trustedPublicKeyBase64?.takeIf { it.isNotBlank() }
            ?: return HookTrustStatus.CANNOT_VERIFY_IN_THIS_BUILD
        return runCatching {
            val payload = Base64.getDecoder().decode(envelope.payloadBase64)
            val verifiedSchemaVersion = NeedleSignature.verifiedSchemaVersion(
                payload = payload,
                signatureBase64 = envelope.signatureBase64,
                publicKeyBase64 = trustedKey,
            )
            if (verifiedSchemaVersion != null) HookTrustStatus.VERIFIED else HookTrustStatus.VERIFICATION_FAILED
        }.getOrDefault(HookTrustStatus.VERIFICATION_FAILED)
    }
}
