package ua.polodarb.gmsflags.data.network.impl.publicapi

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.network.APP_SIGNATURE_SHA256_HEADER
import ua.polodarb.gmsflags.data.network.ServerEnvironment
import ua.polodarb.gmsflags.data.network.NetworkFailure
import ua.polodarb.gmsflags.data.network.NetworkFailureReason
import ua.polodarb.gmsflags.data.network.impl.core.HttpClientFactory
import ua.polodarb.gmsflags.data.network.publicapi.FlagResolveNetResult

class KtorGmsFlagsPublicDataSourceTest {

    @Test
    fun `content endpoints use public routes and deserialize responses`() = runBlocking {
        val requestedPaths = mutableListOf<String>()
        val dataSource = dataSource { request ->
            requestedPaths += request.url.encodedPath
            when (request.url.encodedPath) {
                "/gmsflags/v1/home" -> jsonResponse(INFO_BLOCKS_JSON)
                "/gmsflags/v1/apps" -> jsonResponse(APPS_JSON)
                "/gmsflags/v1/apps/com.google.android.keep" -> jsonResponse(APP_DETAILS_JSON)
                "/gmsflags/v1/apps/com.google.android.keep/recommendations" ->
                    jsonResponse(RECOMMENDATIONS_JSON)
                "/gmsflags/v1/recommendations" -> jsonResponse(RECOMMENDATIONS_JSON)
                "/gmsflags/v1/recommendations/7" -> jsonResponse(RECOMMENDATION_DETAILS_JSON)
                else -> error("Unexpected request: ${request.url}")
            }
        }

        assertEquals("Maintenance", dataSource.getHome().single().message)
        assertEquals("Google Keep", dataSource.getApps().single().displayName)
        val application = dataSource.getApp("com.google.android.keep")
        assertEquals("Keep flag", application.flagCatalog.single().title)
        assertEquals("New", application.flagCatalog.single().badges.single().label)
        assertEquals(7L, dataSource.getAppRecommendations("com.google.android.keep").single().id)
        assertEquals(7L, dataSource.getRecommendations().single().id)
        val details = dataSource.getRecommendation(7)
        assertEquals("A cleaner navigation layout", details.variants.single().description)
        assertEquals("flag_name", details.variants.single().flags.single().flagName)
        assertEquals("Stable", details.variants.single().flags.single().badges.single().label)
        assertEquals(
            "https://example.test/gmsflags/static/preview.png",
            details.screenshots.single(),
        )

        assertEquals(
            listOf(
                "/gmsflags/v1/home",
                "/gmsflags/v1/apps",
                "/gmsflags/v1/apps/com.google.android.keep",
                "/gmsflags/v1/apps/com.google.android.keep/recommendations",
                "/gmsflags/v1/recommendations",
                "/gmsflags/v1/recommendations/7",
            ),
            requestedPaths,
        )
    }

    @Test
    fun `flag resolve returns payload and response etag`() = runBlocking {
        val dataSource = dataSource { request ->
            assertEquals("42", request.url.parameters["versionCode"])
            assertNull(request.headers[HttpHeaders.IfNoneMatch])
            respond(
                content = RESOLVE_JSON,
                status = HttpStatusCode.OK,
                headers = jsonHeaders(HttpHeaders.ETag to "\"config-hash\""),
            )
        }

        val rawResult = dataSource.resolveFlags("com.google.android.keep", 42)
        assertTrue(rawResult is FlagResolveNetResult.Modified)
        val result = rawResult as FlagResolveNetResult.Modified

        assertEquals("\"config-hash\"", result.etag)
        assertEquals("flag_name", result.response.flags.single().flagName)
    }

    @Test
    fun `flag resolve sends cached etag and handles not modified`() = runBlocking {
        val dataSource = dataSource { request ->
            assertEquals("\"cached\"", request.headers[HttpHeaders.IfNoneMatch])
            respond(
                content = "",
                status = HttpStatusCode.NotModified,
                headers = headersOf(HttpHeaders.ETag, "\"cached\""),
            )
        }

        val rawResult = dataSource.resolveFlags("com.google.android.keep", 42, "\"cached\"")
        assertTrue(rawResult is FlagResolveNetResult.NotModified)
        val result = rawResult as FlagResolveNetResult.NotModified

        assertEquals("\"cached\"", result.etag)
    }

    @Test
    fun `hook compatibility route returns supported profile`() = runBlocking {
        val dataSource = dataSource { request ->
            assertEquals(
                "/gmsflags/v1/apps/com.google.android.dialer/hook-compatibility/resolve",
                request.url.encodedPath,
            )
            assertEquals("19896863", request.url.parameters["versionCode"])
            jsonResponse(HOOK_COMPATIBILITY_JSON)
        }

        val response = dataSource.resolveHookCompatibility(
            packageName = "com.google.android.dialer",
            versionCode = 19_896_863,
        )

        assertEquals("SUPPORTED", response.status)
        assertEquals("TYPED_REGISTRY_V1", response.profile?.adapter)
    }

    @Test
    fun `http failure is exposed without leaking ktor exception types`() = runBlocking {
        val dataSource = dataSource {
            respond(content = "not found", status = HttpStatusCode.NotFound)
        }

        val error = runCatching { dataSource.getApps() }.exceptionOrNull()

        assertTrue(error is NetworkFailure)
        assertEquals(NetworkFailureReason.Http, (error as NetworkFailure).reason)
        assertEquals(404, error.statusCode)
    }

    @Test
    fun `sends app signature header when configured`() = runBlocking {
        var capturedHeader: String? = null
        val client = HttpClientFactory.create(
            environment = ServerEnvironment("https://example.test/gmsflags/v1"),
            engine = MockEngine { request ->
                capturedHeader = request.headers[APP_SIGNATURE_SHA256_HEADER]
                jsonResponse(APPS_JSON)
            },
            appSignatureSha256 = "AB:CD:EF",
        )
        val dataSource = KtorGmsFlagsPublicDataSource(
            httpClient = client,
            environment = ServerEnvironment("https://example.test/gmsflags/v1"),
        )

        dataSource.getApps()

        assertEquals("AB:CD:EF", capturedHeader)
    }

    @Test
    fun `omits app signature header when not configured`() = runBlocking {
        var capturedHeader: String? = null
        val dataSource = dataSource { request ->
            capturedHeader = request.headers[APP_SIGNATURE_SHA256_HEADER]
            jsonResponse(APPS_JSON)
        }

        dataSource.getApps()

        assertNull(capturedHeader)
    }

    private fun dataSource(
        handler: suspend io.ktor.client.engine.mock.MockRequestHandleScope.(
            io.ktor.client.request.HttpRequestData,
        ) -> io.ktor.client.request.HttpResponseData,
    ): KtorGmsFlagsPublicDataSource {
        val client = HttpClientFactory.create(
            environment = ServerEnvironment("https://example.test/gmsflags/v1"),
            engine = MockEngine(handler),
        )
        return KtorGmsFlagsPublicDataSource(
            httpClient = client,
            environment = ServerEnvironment("https://example.test/gmsflags/v1"),
        )
    }

    private fun io.ktor.client.engine.mock.MockRequestHandleScope.jsonResponse(content: String) =
        respond(content = content, headers = jsonHeaders())

    private fun jsonHeaders(vararg additional: Pair<String, String>) = headersOf(
        HttpHeaders.ContentType to listOf("application/json"),
        *additional.map { it.first to listOf(it.second) }.toTypedArray(),
    )
}

private const val INFO_BLOCKS_JSON =
    """[{"id":1,"type":"INFO","message":"Maintenance","is_active":true,"sort_order":0}]"""
private const val APPS_JSON =
    """[{"id":2,"package_name":"com.google.android.keep","display_name":"Google Keep","icon_url":null}]"""
private const val APP_DETAILS_JSON = """
    {
      "app":{"id":2,"package_name":"com.google.android.keep","display_name":"Google Keep","icon_url":null},
      "info_blocks":[],
      "flag_catalog":[{"id":3,"flag_name":"flag_name","title":"Keep flag","description":null,"danger_level":"NONE","badges":[{"label":"New"}]}]
    }
"""
private const val RECOMMENDATIONS_JSON = """
    [{"id":7,"status":"PUBLISHED","support_status":"VERIFIED","logo_url":null,"title":"Feature","description":null,"warning_block":null}]
"""
private const val RECOMMENDATION_DETAILS_JSON = """
    {
      "id":7,"app_id":2,"status":"PUBLISHED","support_status":"VERIFIED","logo_url":null,
      "title":"Feature","description":null,"warning_block":null,"info_block":null,
      "screenshots":["/gmsflags/static/preview.png"],"external_link":null,
      "variants":[{"id":8,"label":null,"description":"A cleaner navigation layout","version_constraint":{"type":"UNBOUNDED","min":null,"max":null},
      "flags":[{"flag_name":"flag_name","value_type":"BOOL","value":"true","badges":[{"label":"Stable"}]}]}]
    }
"""
private const val RESOLVE_JSON = """
    {"package":"com.google.android.keep","version_code":42,"flags":[{"flag_name":"flag_name","value_type":"BOOL","value":true}],"config_hash":"config-hash"}
"""
private const val HOOK_COMPATIBILITY_JSON = """
    {"status":"SUPPORTED","profile":{"id":4,"adapter":"TYPED_REGISTRY_V1","version_constraint":{"type":"FROM","min":100},"anchor_groups":[["anchor"]],"enabled":true,"priority":5,"compatibility_status":"VERIFIED","notes":null}}
"""
