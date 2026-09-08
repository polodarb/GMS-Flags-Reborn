package ua.polodarb.gmsflags.data.repository.impl.servermode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.server.PublicContentRepository
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import ua.polodarb.gmsflags.domain.error.AppError
import ua.polodarb.gmsflags.domain.server.content.FaqEntry
import ua.polodarb.gmsflags.domain.server.content.ServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerApplicationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerInfoBlock
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.domain.servermode.ServerMode

class OfflineServerRepositoriesTest {
    @Test
    fun `offline list calls return empty without touching the delegate`() = runTest {
        val delegate = RecordingPublicContentRepository()
        val repository = OfflinePublicContentRepository(delegate, serverMode(offline = true))

        assertEquals(emptyList<ServerInfoBlock>(), repository.getHome().getOrThrow())
        assertEquals(emptyList<FaqEntry>(), repository.getFaq().getOrThrow())
        assertEquals(emptyList<ServerApplication>(), repository.getApplications().getOrThrow())
        assertEquals(emptyList<ServerRecommendationSummary>(), repository.getRecommendations().getOrThrow())
        assertEquals(
            emptyList<ServerRecommendationSummary>(),
            repository.getApplicationRecommendations("com.example").getOrThrow(),
        )
        assertEquals(0, delegate.calls)
    }

    @Test
    fun `offline single-item calls fail as network unavailable`() = runTest {
        val delegate = RecordingPublicContentRepository()
        val repository = OfflinePublicContentRepository(delegate, serverMode(offline = true))

        assertTrue(
            repository.getApplication("com.example").exceptionOrNull() is AppError.NetworkUnavailable,
        )
        assertTrue(repository.getRecommendation(1L).exceptionOrNull() is AppError.NetworkUnavailable)
        assertEquals(0, delegate.calls)
    }

    @Test
    fun `online mode delegates`() = runTest {
        val delegate = RecordingPublicContentRepository()
        val repository = OfflinePublicContentRepository(delegate, serverMode(offline = false))

        repository.getHome()
        repository.getApplication("com.example")

        assertEquals(2, delegate.calls)
    }

    private fun serverMode(offline: Boolean) = object : ServerModeRepository {
        override val mode: StateFlow<ServerMode> =
            MutableStateFlow(ServerMode(offline = offline, notice = null))
        override val hasCachedValue = true
        override suspend fun refresh() = Unit
    }

    private class RecordingPublicContentRepository : PublicContentRepository {
        var calls = 0

        override suspend fun getHome(): Result<List<ServerInfoBlock>> {
            calls++
            return Result.success(emptyList())
        }

        override suspend fun getFaq(): Result<List<FaqEntry>> {
            calls++
            return Result.success(emptyList())
        }

        override suspend fun getApplications(): Result<List<ServerApplication>> {
            calls++
            return Result.success(emptyList())
        }

        override suspend fun getApplication(packageName: String): Result<ServerApplicationDetails> {
            calls++
            return Result.failure(AppError.NotFound)
        }

        override suspend fun getApplicationRecommendations(
            packageName: String,
        ): Result<List<ServerRecommendationSummary>> {
            calls++
            return Result.success(emptyList())
        }

        override suspend fun getRecommendations(): Result<List<ServerRecommendationSummary>> {
            calls++
            return Result.success(emptyList())
        }

        override suspend fun getRecommendation(id: Long): Result<ServerRecommendationDetails> {
            calls++
            return Result.failure(AppError.NotFound)
        }
    }
}
