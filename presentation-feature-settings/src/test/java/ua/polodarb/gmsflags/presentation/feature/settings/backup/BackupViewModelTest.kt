package ua.polodarb.gmsflags.presentation.feature.settings.backup

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.backup.BackupPackage
import ua.polodarb.gmsflags.domain.backup.BackupRestoreResult
import ua.polodarb.gmsflags.domain.backup.FlagsBackup
import ua.polodarb.gmsflags.domain.backup.FlagsBackupCodec
import ua.polodarb.gmsflags.domain.backup.FlagsBackupService
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.settings.R

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {
    @Test
    fun `import only previews until user requests restore`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            val vm = fixture.vm(StandardTestDispatcher(testScheduler), "file")
            advanceUntilIdle()
            assertEquals(fixture.backup, vm.state.value.backup)
            assertEquals(0, fixture.restores)
            vm.restore()
            vm.restore()
            advanceUntilIdle()
            assertEquals(1, fixture.restores)
            assertEquals(1, vm.state.value.result?.restoredFlags)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `cancelled save does not write and allows next export`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            val vm = fixture.vm(StandardTestDispatcher(testScheduler))
            vm.export()
            advanceUntilIdle()
            assertTrue(vm.state.value.busy)
            vm.save(null)
            assertFalse(vm.state.value.busy)
            assertEquals(0, fixture.writes)
            vm.export()
            advanceUntilIdle()
            vm.save("output")
            advanceUntilIdle()
            assertEquals(1, fixture.writes)
            assertEquals(R.string.backup_saved, vm.state.value.message)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `invalid import never restores anything`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            fixture.invalid = true
            val vm = fixture.vm(StandardTestDispatcher(testScheduler), "file")
            advanceUntilIdle()
            vm.restore()
            advanceUntilIdle()
            assertNull(vm.state.value.backup)
            assertEquals(R.string.backup_import_failed, vm.state.value.message)
            assertEquals(0, fixture.restores)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `saved summary appears only after file write finishes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            fixture.writeGate = CompletableDeferred()
            val vm = fixture.vm(StandardTestDispatcher(testScheduler))
            vm.export()
            advanceUntilIdle()
            assertNull(vm.state.value.backup)
            vm.save("output")
            runCurrent()
            assertTrue(vm.state.value.busy)
            assertNull(vm.state.value.savedFileName)
            fixture.writeGate!!.complete(Unit)
            advanceUntilIdle()
            assertEquals(fixture.backup, vm.state.value.backup)
            assertEquals("renamed.gmsbackup", vm.state.value.savedFileName)
            assertFalse(vm.state.value.busy)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `failed save has no success summary and export can be retried`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            fixture.failWrite = true
            val vm = fixture.vm(StandardTestDispatcher(testScheduler))
            vm.export()
            advanceUntilIdle()
            vm.save("output")
            advanceUntilIdle()
            assertEquals(R.string.backup_save_failed, vm.state.value.message)
            assertNull(vm.state.value.savedFileName)
            assertNull(vm.state.value.backup)
            fixture.failWrite = false
            vm.export()
            advanceUntilIdle()
            vm.save("output")
            advanceUntilIdle()
            assertEquals(fixture.backup, vm.state.value.backup)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `picker result after process recreation uses the prepared export`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            val handle = SavedStateHandle()
            val original = fixture.vm(StandardTestDispatcher(testScheduler), handle = handle)
            original.export()
            advanceUntilIdle()
            original.pickerLaunched()
            val restoredHandle = SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })
            val recreated = fixture.vm(StandardTestDispatcher(testScheduler), handle = restoredHandle)
            assertTrue(recreated.state.value.busy)
            recreated.save("output")
            advanceUntilIdle()
            assertEquals(1, fixture.writes)
            assertEquals(fixture.backup, recreated.state.value.backup)
            assertTrue(recreated.state.value.summaryVisible)
            assertTrue(restoredHandle.keys().isEmpty())
            assertTrue(fixture.prepared.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `dismissing success consumes it while retaining the saved summary data`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            val vm = fixture.vm(StandardTestDispatcher(testScheduler))
            vm.export()
            advanceUntilIdle()
            vm.save("output")
            advanceUntilIdle()
            assertTrue(vm.state.value.summaryVisible)
            vm.dismissSummary()
            assertFalse(vm.state.value.summaryVisible)
            assertEquals("renamed.gmsbackup", vm.state.value.savedFileName)
            assertEquals(fixture.backup, vm.state.value.backup)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `recreation before picker launch reissues the request without rereading flags`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val fixture = Fixture()
            val handle = SavedStateHandle()
            fixture.vm(StandardTestDispatcher(testScheduler), handle = handle).export()
            advanceUntilIdle()
            val recreated = fixture.vm(StandardTestDispatcher(testScheduler), handle = handle)
            assertEquals(Unit, recreated.saveRequest.first())
            assertEquals(1, fixture.exports)
            recreated.save(null)
            advanceUntilIdle()
            assertTrue(fixture.prepared.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class Fixture {
        val backup = FlagsBackup(
            listOf(
                BackupPackage(
                    "com.test.app",
                    "custom",
                    listOf(FlagOverride("flag", FlagType.String, "value")),
                )
            )
        )
        val prepared = mutableMapOf<String, String>()
        var exports = 0
        var restores = 0
        var writes = 0
        var invalid = false
        var failWrite = false
        var writeGate: CompletableDeferred<Unit>? = null

        fun vm(
            dispatcher: CoroutineDispatcher,
            uri: String? = null,
            handle: SavedStateHandle = SavedStateHandle(),
        ) = BackupViewModel(
            object : FlagsBackupService {
                override suspend fun availableApps() = setOf("com.test.app")

                override suspend fun export(): FlagsBackup {
                    exports++
                    return backup
                }

                override suspend fun restore(backup: FlagsBackup): BackupRestoreResult {
                    restores++
                    return BackupRestoreResult(1, emptyList(), emptyList(), emptyList())
                }
            },
            object : FlagsBackupCodec {
                override fun isBackup(xml: String) = true

                override fun encode(backup: FlagsBackup) = "encoded"

                override fun decode(xml: String): FlagsBackup {
                    check(!invalid)
                    return backup
                }
            },
            object : BackupDocumentStore {
                override suspend fun read(uri: String) = "document"

                override suspend fun displayName(uri: String) = "renamed.gmsbackup"

                override suspend fun write(uri: String, xml: String) {
                    writeGate?.await()
                    check(!failWrite)
                    assertEquals("encoded", xml)
                    writes++
                }
            },
            object : BackupExportStore {
                override suspend fun prepare(xml: String): String {
                    prepared["id"] = xml
                    return "id"
                }

                override suspend fun read(id: String) = checkNotNull(prepared[id])

                override suspend fun delete(id: String) {
                    prepared.remove(id)
                }
            },
            uri,
            dispatcher,
            handle,
        )
    }
}
