package ua.polodarb.gmsflags.domain.server.content

fun interface GetHomeContent {
    suspend operator fun invoke(): Result<List<ServerInfoBlock>>
}

fun interface GetFaq {
    suspend operator fun invoke(): Result<List<FaqEntry>>
}

fun interface GetServerApplications {
    suspend operator fun invoke(): Result<List<ServerApplication>>
}

fun interface GetServerApplication {
    suspend operator fun invoke(packageName: String): Result<ServerApplicationDetails>
}

fun interface GetApplicationRecommendations {
    suspend operator fun invoke(packageName: String): Result<List<ServerRecommendationSummary>>
}

fun interface GetRecommendations {
    suspend operator fun invoke(): Result<List<ServerRecommendationSummary>>
}

fun interface GetRecommendationFeed {
    suspend operator fun invoke(): Result<List<ServerRecommendationFeedItem>>
}

fun interface GetRecommendationDetails {
    suspend operator fun invoke(id: Long): Result<ServerRecommendationDetails>
}

fun interface GetRecommendationExperience {
    suspend operator fun invoke(id: Long): Result<RecommendationExperience>
}
