package ua.polodarb.gmsflags.data.network.publicapi

import ua.polodarb.gmsflags.data.network.publicapi.model.AppDetailsNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.AppNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.FaqEntryNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.HookCompatibilityResolveNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.InfoBlockNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationDetailNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationSummaryNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ReportRequestNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ReportResponseNetModel

import ua.polodarb.gmsflags.data.network.publicapi.model.CommunityPackageNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.CommunitySubmitRequestNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.CommunityReportRequestNetModel

interface GmsFlagsPublicDataSource {
    suspend fun getHome(): List<InfoBlockNetModel>

    suspend fun getFaq(): List<FaqEntryNetModel>

    suspend fun getApps(): List<AppNetModel>

    suspend fun getApp(packageName: String): AppDetailsNetModel

    suspend fun getAppRecommendations(packageName: String): List<RecommendationSummaryNetModel>

    suspend fun getRecommendations(): List<RecommendationSummaryNetModel>

    suspend fun getRecommendation(id: Long): RecommendationDetailNetModel

    suspend fun getCommunityPackages(query: String? = null): List<CommunityPackageNetModel>

    suspend fun getCommunityPackage(id: Long): CommunityPackageNetModel

    suspend fun submitCommunityPackage(request: CommunitySubmitRequestNetModel): Boolean

    suspend fun reportCommunityPackage(request: CommunityReportRequestNetModel): Boolean

    suspend fun resolveFlags(
        packageName: String,
        versionCode: Long,
        etag: String? = null,
    ): FlagResolveNetResult

    suspend fun resolveHookCompatibility(
        packageName: String,
        versionCode: Long,
    ): HookCompatibilityResolveNetModel

    /**
     * Submits a user-authored problem report with attached diagnostics to the public `/reports`
     * endpoint. The endpoint is public (no auth), like [resolveFlags]. Returns the optional
     * `{"id":...}` acknowledgement when the backend provides one, or null when the body is empty.
     */
    suspend fun submitReport(request: ReportRequestNetModel): ReportResponseNetModel?
}

