package ua.polodarb.gmsflags.domain.server.content

/**
 * How much a [RecommendationVariantHook]'s signature can be trusted, from the perspective of THIS
 * build/device. This is intentionally not a boolean: [NOT_SIGNED] and [CANNOT_VERIFY_IN_THIS_BUILD]
 * are both "we didn't check" states that must never be presented as alarming, while
 * [VERIFICATION_FAILED] is the one state that must always be presented as alarming. Never collapse
 * these into a single "verified" flag - that would either hide a real signature mismatch or falsely
 * claim verification happened when it didn't.
 */
enum class HookTrustStatus {
    /** The recipe isn't published/signed on the backend yet. Normal, not alarming. */
    NOT_SIGNED,

    /** The envelope's signature was checked against the trusted key and matched. */
    VERIFIED,

    /** The envelope's signature was checked against the trusted key and did NOT match. Alarming. */
    VERIFICATION_FAILED,

    /** This build has no local trusted key to check against (e.g. a release build). Not alarming. */
    CANNOT_VERIFY_IN_THIS_BUILD,
}

/**
 * Pure, synchronous trust check for a single hook. Implementations must never throw and must never
 * report [HookTrustStatus.VERIFIED] unless the signature was actually, cryptographically checked
 * against a trusted key.
 */
fun interface VerifyHookTrust {
    operator fun invoke(hook: RecommendationVariantHook): HookTrustStatus
}
