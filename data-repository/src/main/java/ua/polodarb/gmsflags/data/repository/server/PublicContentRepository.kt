package ua.polodarb.gmsflags.data.repository.server

import ua.polodarb.gmsflags.domain.server.content.FaqEntry
import ua.polodarb.gmsflags.domain.server.content.ServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerApplicationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerInfoBlock
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary

interface PublicContentRepository {
    suspend fun getHome(): Result<List<ServerInfoBlock>>

    suspend fun getFaq(): Result<List<FaqEntry>>

    suspend fun getApplications(): Result<List<ServerApplication>>

    suspend fun getApplication(packageName: String): Result<ServerApplicationDetails>

    suspend fun getApplicationRecommendations(
        packageName: String,
    ): Result<List<ServerRecommendationSummary>>

    suspend fun getRecommendations(): Result<List<ServerRecommendationSummary>>

    suspend fun getRecommendation(id: Long): Result<ServerRecommendationDetails>
}
