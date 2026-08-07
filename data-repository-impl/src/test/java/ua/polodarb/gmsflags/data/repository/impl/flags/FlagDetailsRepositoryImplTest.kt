package ua.polodarb.gmsflags.data.repository.impl.flags

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagRecord
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagsDataSource
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeOverrideRecord
import ua.polodarb.gmsflags.data.repository.flags.FlagOverridesChangeBus
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange
import ua.polodarb.gmsflags.domain.flags.FlagType

class FlagDetailsRepositoryImplTest {
    @Test
    fun `large batch write restarts target once`() = runBlocking {
        val source = FakeFlagsDataSource()
        val changeBus = RecordingChangeBus()
        var restartCount = 0
        val repository = FlagDetailsRepositoryImpl(
            source,
            TargetProcessRestarter { restartCount++; Result.success(Unit) },
            changeBus,
        )

        val result = repository.applyOverrides(
            "com.google.test",
            "test",
            List(263) { index ->
                FlagOverride("flag_$index", FlagType.Boolean, "1")
            },
        )

        assertTrue(result.isSuccess)
        assertEquals(1, source.writeCount)
        assertEquals(1, restartCount)
        assertEquals(263, (changeBus.recorded.single() as FlagOverridesChange.Applied).overrides.size)
    }

    @Test
    fun `failed write does not restart target`() = runBlocking {
        val source = FakeFlagsDataSource(writeResult = Result.failure(IllegalStateException("write")))
        val changeBus = RecordingChangeBus()
        var restartCount = 0
        val repository = FlagDetailsRepositoryImpl(
            source,
            TargetProcessRestarter { restartCount++; Result.success(Unit) },
            changeBus,
        )

        val result = repository.applyOverrides(
            "com.google.test",
            "test",
            listOf(FlagOverride("first", FlagType.Boolean, "1")),
        )

        assertTrue(result.isFailure)
        assertEquals(0, restartCount)
        assertTrue(changeBus.recorded.isEmpty())
    }
}

private class RecordingChangeBus : FlagOverridesChangeBus {
    private val mutableChanges = MutableSharedFlow<FlagOverridesChange>()
    override val changes: SharedFlow<FlagOverridesChange> = mutableChanges
    val recorded = mutableListOf<FlagOverridesChange>()

    override suspend fun notifyChanged(change: FlagOverridesChange) {
        recorded += change
    }
}

private class FakeFlagsDataSource(
    private val writeResult: Result<Unit> = Result.success(Unit),
) : PhenotypeFlagsDataSource {
    var writeCount = 0

    override suspend fun readFlags(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<List<PhenotypeFlagRecord>> = Result.success(emptyList())

    override suspend fun writeOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<PhenotypeOverrideRecord>,
    ): Result<Unit> {
        writeCount++
        return writeResult
    }

    override suspend fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<Unit> = Result.success(Unit)
}
