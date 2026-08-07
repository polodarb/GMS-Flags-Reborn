package ua.polodarb.gmsflags.domain.flags

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository

class ApplyFlagOverridesUseCaseTest {
    @Test
    fun `normalizes values and keeps the latest override`() = runBlocking {
        val repository = RecordingRepository()
        val useCase = ApplyFlagOverridesUseCase(repository)

        val result = useCase(
            "com.google.test",
            "test#com.google.test",
            listOf(
                FlagOverride(" flag ", FlagType.Boolean, "true"),
                FlagOverride("flag", FlagType.Boolean, "0"),
                FlagOverride("count", FlagType.Integer, " 12 "),
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(
                FlagOverride("flag", FlagType.Boolean, "0"),
                FlagOverride("count", FlagType.Integer, "12"),
            ),
            repository.applied,
        )
    }

    @Test
    fun `invalid Android package is returned as failure without repository access`() = runBlocking {
        val repository = RecordingRepository()

        val result = ApplyFlagOverridesUseCase(repository)(
            "com.google.test;stop",
            "test",
            listOf(FlagOverride("flag", FlagType.Boolean, "1")),
        )

        assertTrue(result.isFailure)
        assertTrue(repository.applied.isEmpty())
    }
}

private class RecordingRepository : FlagDetailsRepository {
    var applied = emptyList<FlagOverride>()

    override suspend fun getFlags(androidPackageName: String, phenotypePackageName: String) =
        Result.success(emptyList<PhenotypeFlag>())

    override suspend fun applyOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<FlagOverride>,
    ): Result<Unit> {
        applied = overrides
        return Result.success(Unit)
    }

    override suspend fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ) = Result.success(Unit)

    override suspend fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ) = Result.success(Unit)

    override suspend fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ) = Result.success(Unit)
}
