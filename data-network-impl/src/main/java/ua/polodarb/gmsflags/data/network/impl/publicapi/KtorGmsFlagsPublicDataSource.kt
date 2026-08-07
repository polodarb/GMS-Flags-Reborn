package ua.polodarb.gmsflags.data.network.impl.publicapi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import ua.polodarb.gmsflags.data.network.NetworkFailure
import ua.polodarb.gmsflags.data.network.NetworkFailureReason
import ua.polodarb.gmsflags.data.network.ServerEnvironment
import ua.polodarb.gmsflags.data.network.impl.core.safeApiCall
import ua.polodarb.gmsflags.data.network.publicapi.FlagResolveNetResult
import ua.polodarb.gmsflags.data.network.publicapi.GmsFlagsPublicDataSource
import ua.polodarb.gmsflags.data.network.publicapi.model.AppDetailsNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.AppNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.FaqEntryNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.HookCompatibilityResolveNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.InfoBlockNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationDetailNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationSummaryNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ReportRequestNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ReportResponseNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ResolveResponseNetModel

internal class KtorGmsFlagsPublicDataSource(
    private val httpClient: HttpClient,
    private val environment: ServerEnvironment,
) : GmsFlagsPublicDataSource {

    override suspend fun getHome(): List<InfoBlockNetModel> = httpClient.safeApiCall {
        get { url { appendPathSegments(PublicApiRoutes.HOME) } }
    }

    override suspend fun getFaq(): List<FaqEntryNetModel> = httpClient.safeApiCall {
        get { url { appendPathSegments(PublicApiRoutes.FAQ) } }
    }

    override suspend fun getApps(): List<AppNetModel> = httpClient.safeApiCall {
        get { url { appendPathSegments(PublicApiRoutes.APPS) } }
    }

    override suspend fun getApp(packageName: String): AppDetailsNetModel = httpClient.safeApiCall {
        get {
            url { appendPathSegments(PublicApiRoutes.APPS, packageName.requirePackageName()) }
        }
    }

    override suspend fun getAppRecommendations(
        packageName: String,
    ): List<RecommendationSummaryNetModel> =
        httpClient.safeApiCall<List<RecommendationSummaryNetModel>> {
            get {
                url {
                    appendPathSegments(
                        PublicApiRoutes.APPS,
                        packageName.requirePackageName(),
                        PublicApiRoutes.RECOMMENDATIONS,
                    )
                }
            }
        }.map(::resolveMedia)

    override suspend fun getRecommendations(): List<RecommendationSummaryNetModel> =
        httpClient.safeApiCall<List<RecommendationSummaryNetModel>> {
            get { url { appendPathSegments(PublicApiRoutes.RECOMMENDATIONS) } }
        }.map(::resolveMedia)

    override suspend fun getRecommendation(id: Long): RecommendationDetailNetModel =
        httpClient.safeApiCall<RecommendationDetailNetModel> {
            get {
                url {
                    appendPathSegments(PublicApiRoutes.RECOMMENDATIONS, id.requireId().toString())
                }
            }
        }.let(::resolveMedia)

    override suspend fun resolveFlags(
        packageName: String,
        versionCode: Long,
        etag: String?,
    ): FlagResolveNetResult {
        return httpClient.safeApiCall(
            acceptedStatus = { status ->
                status == HttpStatusCode.OK || status == HttpStatusCode.NotModified
            },
            request = {
                get {
                    url {
                        appendPathSegments(
                            PublicApiRoutes.APPS,
                            packageName.requirePackageName(),
                            PublicApiRoutes.RESOLVE,
                        )
                    }
                    parameter(VERSION_CODE_QUERY, versionCode.requireVersionCode())
                    etag?.takeIf(String::isNotBlank)?.let {
                        header(HttpHeaders.IfNoneMatch, it)
                    }
                }
            },
            transform = { response ->
                val responseEtag = response.headers[HttpHeaders.ETag]
                when (response.status) {
                    HttpStatusCode.OK -> FlagResolveNetResult.Modified(
                        response = response.body<ResolveResponseNetModel>(),
                        etag = responseEtag,
                    )
                    HttpStatusCode.NotModified -> FlagResolveNetResult.NotModified(
                        etag = responseEtag ?: etag,
                    )
                    else -> throw NetworkFailure(NetworkFailureReason.Unknown)
                }
            },
        )
    }

    override suspend fun resolveHookCompatibility(
        packageName: String,
        versionCode: Long,
    ): HookCompatibilityResolveNetModel = httpClient.safeApiCall {
        get {
            url {
                appendPathSegments(
                    PublicApiRoutes.APPS,
                    packageName.requirePackageName(),
                    PublicApiRoutes.HOOK_COMPATIBILITY,
                    PublicApiRoutes.RESOLVE,
                )
            }
            parameter(VERSION_CODE_QUERY, versionCode.requireVersionCode())
        }
    }

    override suspend fun submitReport(request: ReportRequestNetModel): ReportResponseNetModel? =
        httpClient.safeApiCall(
            request = {
                post {
                    url { appendPathSegments(PublicApiRoutes.REPORTS) }
                    setBody(request)
                }
            },
            transform = { response ->
                runCatching { response.body<ReportResponseNetModel>() }.getOrNull()
            },
        )

    private fun resolveMedia(model: RecommendationSummaryNetModel) = model.copy(
        logoUrl = environment.resolvePublicUrl(model.logoUrl),
    )

    private fun resolveMedia(model: RecommendationDetailNetModel) = model.copy(
        logoUrl = environment.resolvePublicUrl(model.logoUrl),
        screenshots = model.screenshots.mapNotNull(environment::resolvePublicUrl),
    )
}

private fun String.requirePackageName(): String = trim().also {
    require(it.isNotEmpty()) { "Package name cannot be blank" }
}

private fun Long.requireId(): Long = also {
    require(it > 0L) { "Recommendation id must be positive" }
}

private fun Long.requireVersionCode(): Long = also {
    require(it >= 0L) { "Version code cannot be negative" }
}

private const val VERSION_CODE_QUERY = "versionCode"
