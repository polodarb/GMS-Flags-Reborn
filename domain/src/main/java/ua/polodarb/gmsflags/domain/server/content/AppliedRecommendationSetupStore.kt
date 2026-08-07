package ua.polodarb.gmsflags.domain.server.content

/**
 * Exact local snapshot of what a recommendation last installed.
 *
 * The server-side variant may be edited after it has been applied. Keeping the installed identities
 * locally lets Reapply and Disable remove the old setup without deleting unrelated manual overrides
 * or another recommendation for the same target app.
 *
 * Flag drift is already detected live (the current server override is compared against the live
 * device flag value on every status refresh, see [resolveRecommendationApplicationStatus]) - the
 * flag names here are only ever used to know what to delete. Hooks have no equivalent live read:
 * nothing on-device exposes what hook content is currently installed, so [AppliedHookRef] records
 * the payload hash of what was actually written, to be compared later against the server's current
 * hash for that recipe (see [resolveHookApplicationStatus]).
 */
data class AppliedRecommendationSetup(
    val recommendationId: Long,
    val androidPackageName: String,
    val flagNamesByPackage: Map<String, Set<String>>,
    val hooks: Set<AppliedHookRef>,
)

data class AppliedHookRef(
    val recipeId: Long,
    val payloadSha256: String,
    val required: Boolean,
)

interface AppliedRecommendationSetupStore {
    suspend fun read(recommendationId: Long): Result<AppliedRecommendationSetup?>

    suspend fun write(setup: AppliedRecommendationSetup): Result<Unit>

    suspend fun clear(recommendationId: Long): Result<Unit>

    suspend fun clearAll(): Result<Unit>
}
