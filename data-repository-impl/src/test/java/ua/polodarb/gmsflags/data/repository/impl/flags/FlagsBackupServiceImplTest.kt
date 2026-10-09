package ua.polodarb.gmsflags.data.repository.impl.flags

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.flags.FlagOverridesChangeBus
import ua.polodarb.gmsflags.data.repository.impl.apps.reader.InstalledApplicationMetadata
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagRecord
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagsDataSource
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeOverrideRecord
import ua.polodarb.gmsflags.data.repository.phenotype.SavedOverrideRecord
import ua.polodarb.gmsflags.data.repository.phenotype.SavedOverridesDataSource
import ua.polodarb.gmsflags.data.repository.settings.datasource.OverrideControlDataSource
import ua.polodarb.gmsflags.data.repository.settings.repository.OverrideControlRepository
import ua.polodarb.gmsflags.domain.backup.BackupPackage
import ua.polodarb.gmsflags.domain.backup.FlagsBackup
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.settings.OverrideControlState

class FlagsBackupServiceImplTest {
    @Test
    fun `exports custom saved packages without consulting base flags`() = runBlocking {
        val fixture = Fixture()
        fixture.records["com.test.one"] = listOf(
            SavedOverrideRecord("undiscovered", PhenotypeOverrideRecord("custom", 3, "value"))
        )
        val backup = fixture.service.export()
        assertEquals("undiscovered", backup.packages.single().phenotypePackageName)
        assertEquals("value", backup.packages.single().flags.single().value)
    }

    @Test
    fun `export fails rather than producing incomplete backup`() = runBlocking {
        val fixture = Fixture()
        fixture.readFailure = true
        assertTrue(runCatching { fixture.service.export() }.isFailure)
    }

    @Test
    fun `merge preserves unrelated flags and pause state and restarts once per app`() = runBlocking {
        val fixture = Fixture()
        fixture.values["com.test.one" to "custom"] = mutableMapOf("unrelated" to "keep", "flag" to "old")
        fixture.paused.value = OverrideControlState(paused = true)
        val backup = FlagsBackup(
            listOf(
                pkg("com.test.one", "custom"),
                pkg("com.test.one", "other"),
                pkg("com.missing.app", "custom"),
            )
        )
        val result = fixture.service.restore(backup)
        assertEquals(2, result.restoredFlags)
        assertEquals(1, result.skippedPackages.size)
        assertTrue(result.failedPackages.isEmpty())
        assertEquals(
            mapOf("unrelated" to "keep", "flag" to "new"),
            fixture.values["com.test.one" to "custom"],
        )
        assertEquals(listOf("com.test.one"), fixture.restarts)
        assertEquals(listOf(true), fixture.pauseWrites)
        assertEquals(2, fixture.notifications.size)
        assertTrue(fixture.refreshed)
    }

    @Test
    fun `partial write failure is reported and other apps still restore`() = runBlocking {
        val fixture = Fixture()
        fixture.failingPackage = "broken"
        val result = fixture.service.restore(
            FlagsBackup(listOf(pkg("com.test.one", "broken"), pkg("com.test.two", "working")))
        )
        assertEquals(1, result.restoredFlags)
        assertEquals("broken", result.failedPackages.single().phenotypePackageName)
        assertEquals(setOf("com.test.one", "com.test.two"), fixture.restarts.toSet())
        assertEquals(2, fixture.notifications.size)
    }

    @Test
    fun `restart failure reports saved flags separately`() = runBlocking {
        val fixture = Fixture()
        fixture.restartFailure = true
        val result = fixture.service.restore(FlagsBackup(listOf(pkg("com.test.one", "custom"))))
        assertEquals(1, result.restoredFlags)
        assertTrue(result.failedPackages.isEmpty())
        assertEquals(listOf("com.test.one"), result.restartFailedApps)
    }

    @Test
    fun `completed restore survives thrown final refresh failure`() = runBlocking {
        val fixture = Fixture()
        fixture.refreshThrows = true
        val result = fixture.service.restore(FlagsBackup(listOf(pkg("com.test.one", "custom"))))
        assertEquals(1, result.restoredFlags)
        assertTrue(result.failedPackages.isEmpty())
        assertTrue(result.refreshFailed)
        assertEquals("new", fixture.values["com.test.one" to "custom"]?.get("flag"))
        assertEquals(listOf("com.test.one"), fixture.restarts)
    }

    @Test
    fun `completed restore reports returned final refresh failure separately`() = runBlocking {
        val fixture = Fixture()
        fixture.refreshFailure = true
        val result = fixture.service.restore(FlagsBackup(listOf(pkg("com.test.one", "custom"))))
        assertEquals(1, result.restoredFlags)
        assertTrue(result.refreshFailed)
        assertTrue(result.failedPackages.isEmpty())
    }

    private fun pkg(app: String, name: String) =
        BackupPackage(app, name, listOf(FlagOverride("flag", FlagType.String, "new")))
}

private class Fixture {
    val records = mutableMapOf<String, List<SavedOverrideRecord>>()
    val values = mutableMapOf<Pair<String, String>, MutableMap<String, String>>()
    val restarts = mutableListOf<String>()
    val notifications = mutableListOf<FlagOverridesChange>()
    val pauseWrites = mutableListOf<Boolean>()
    val paused = MutableStateFlow(OverrideControlState())
    var refreshed = false
    var refreshThrows = false
    var refreshFailure = false
    var readFailure = false
    var restartFailure = false
    var failingPackage: String? = null
    val service = FlagsBackupServiceImpl(
        object : SavedOverridesDataSource {
            override suspend fun readAll(
                androidPackageName: String
            ): Result<List<SavedOverrideRecord>> = if (readFailure) Result.failure(IllegalStateException())
                else Result.success(records[androidPackageName].orEmpty())
        },
        object : PhenotypeFlagsDataSource {
            override suspend fun readFlags(
                androidPackageName: String,
                phenotypePackageName: String,
            ): Result<List<PhenotypeFlagRecord>> = error("Must not read base flags")

            override suspend fun writeOverrides(
                androidPackageName: String,
                phenotypePackageName: String,
                overrides: List<PhenotypeOverrideRecord>,
            ): Result<Unit> {
                if (phenotypePackageName == failingPackage) {
                    return Result.failure(IllegalStateException())
                }
                val flags = values.getOrPut(androidPackageName to phenotypePackageName) {
                    mutableMapOf()
                }
                overrides.forEach { flags[it.name] = it.value }
                return Result.success(Unit)
            }

            override suspend fun deleteOverride(
                androidPackageName: String,
                phenotypePackageName: String,
                flagName: String,
            ): Result<Unit> = error("Must not delete flags")

            override suspend fun deleteOverrides(
                androidPackageName: String,
                phenotypePackageName: String,
                flagNames: List<String>,
            ): Result<Unit> = error("Must not delete flags")

            override suspend fun deletePackageOverrides(
                androidPackageName: String,
                phenotypePackageName: String,
            ): Result<Unit> = error("Must not delete flags")
        },
        { names -> names.associateWith { InstalledApplicationMetadata(it, it, null, 1L, 0L) } },
        { setOf("com.test.one", "com.test.two") },
        {
            restarts += it
            if (restartFailure) {
                Result.failure(IllegalStateException())
            } else {
                Result.success(Unit)
            }
        },
        object : FlagOverridesChangeBus {
            override val changes = MutableSharedFlow<FlagOverridesChange>()

            override suspend fun notifyChanged(change: FlagOverridesChange) {
                notifications += change
            }
        },
        object : OverrideControlDataSource {
            override suspend fun readOverrideCount(androidPackageNames: List<String>) = Result.success(0)

            override suspend fun readPaused(androidPackageNames: List<String>) = Result.success(paused.value.paused)

            override suspend fun setPaused(
                androidPackageNames: List<String>,
                paused: Boolean,
            ): Result<Unit> {
                pauseWrites += paused
                return Result.success(Unit)
            }

            override suspend fun deleteAll(androidPackageNames: List<String>): Result<Unit> =
                error("Must not delete hooks or flags")
        },
        object : OverrideControlRepository {
            override val state = paused

            override suspend fun refresh(): Result<Unit> {
                refreshed = true
                check(!refreshThrows)
                return if (refreshFailure) Result.failure(IllegalStateException())
                else Result.success(Unit)
            }

            override suspend fun setPaused(paused: Boolean): Result<Unit> =
                error("Must preserve global pause preference")

            override suspend fun deleteAll(): Result<Unit> = error("Must not delete hooks or flags")

            override suspend fun deleteAll(androidPackageName: String): Result<Unit> =
                error("Must not delete hooks or flags")
        },
    )
}
