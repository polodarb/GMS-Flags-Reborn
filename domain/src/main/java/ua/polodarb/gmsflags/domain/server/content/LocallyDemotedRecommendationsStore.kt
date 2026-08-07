package ua.polodarb.gmsflags.domain.server.content

/**
 * Recommendation ids the user has swiped out of the pinned/featured section into the regular
 * per-app stack. Purely a local UI preference - the server's own [ServerRecommendationSummary.pinned]
 * is unaffected and unaware of this.
 */
interface LocallyDemotedRecommendationsStore {
    suspend fun read(): Result<Set<Long>>

    suspend fun demote(recommendationId: Long): Result<Unit>

    suspend fun restore(recommendationId: Long): Result<Unit>
}
