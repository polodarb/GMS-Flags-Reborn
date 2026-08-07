package ua.polodarb.gmsflags.data.repository.impl.server.repository

import ua.polodarb.gmsflags.data.network.publicapi.GmsFlagsPublicDataSource
import ua.polodarb.gmsflags.data.repository.impl.common.networkResult
import ua.polodarb.gmsflags.data.repository.impl.server.mapper.toDomain
import ua.polodarb.gmsflags.data.repository.server.PublicContentRepository

class PublicContentRepositoryImpl(
    private val dataSource: GmsFlagsPublicDataSource,
) : PublicContentRepository {
    override suspend fun getHome() = networkResult {
        dataSource.getHome().map { it.toDomain() }
    }

    override suspend fun getFaq() = networkResult {
        dataSource.getFaq().map { it.toDomain() }
    }

    override suspend fun getApplications() = networkResult {
        dataSource.getApps().map { it.toDomain() }
    }

    override suspend fun getApplication(packageName: String) = networkResult {
        dataSource.getApp(packageName).toDomain()
    }

    override suspend fun getApplicationRecommendations(packageName: String) = networkResult {
        dataSource.getAppRecommendations(packageName).map { it.toDomain() }
    }

    override suspend fun getRecommendations() = networkResult {
        dataSource.getRecommendations().map { it.toDomain() }
    }

    override suspend fun getRecommendation(id: Long) = networkResult {
        dataSource.getRecommendation(id).toDomain()
    }
}
