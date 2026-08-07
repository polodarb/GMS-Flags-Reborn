package ua.polodarb.gmsflags.data.phenotype.runtime

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeFlagOverrideStoreTest {
    private val file = File("runtime-overrides.db")
    private val database = FakeDatabase()
    private val access = FakeFileAccess()
    private val store = RuntimeFlagOverrideStore(
        database = database,
        locator = RuntimeOverrideDatabaseLocator { file },
        fileAccess = access,
    )

    @Test
    fun `restores file context only on first write for target`() {
        val override = RuntimeFlagOverride("phenotype", "flag", 0, "1")

        store.write("android", "phenotype", listOf(override))
        store.write("android", "phenotype", listOf(override))

        assertEquals(listOf(true, false), access.restoreContextCalls)
    }

    @Test
    fun `missing database delete does not prepare file permissions`() {
        database.deleteResult = false

        store.delete("android", "phenotype", "flag")

        assertTrue(access.restoreContextCalls.isEmpty())
    }

    @Test
    fun `delete all clears the runtime database and preserves file ownership`() {
        store.deleteAll("android")

        assertEquals(listOf(file), database.deleteAllFiles)
        assertEquals(listOf(false), access.restoreContextCalls)
    }

    private class FakeDatabase : RuntimeOverrideDatabase {
        var deleteResult = true
        val deleteAllFiles = mutableListOf<File>()

        override fun read(file: File, phenotypePackageName: String) = emptyList<RuntimeFlagOverride>()
        override fun write(
            file: File,
            phenotypePackageName: String,
            overrides: List<RuntimeFlagOverride>,
        ) = Unit

        override fun writeMicroHooks(
            file: File,
            androidPackageName: String,
            hooks: List<RuntimeMicroHookOverride>,
        ) = Unit

        override fun delete(file: File, phenotypePackageName: String, flagName: String) =
            deleteResult

        override fun delete(
            file: File,
            phenotypePackageName: String,
            flagNames: List<String>,
        ) = deleteResult

        override fun deletePackage(file: File, phenotypePackageName: String) = deleteResult

        override fun deleteMicroHooks(
            file: File,
            androidPackageName: String,
            recipeIds: List<Long>,
        ) = deleteResult

        override fun deleteAll(file: File): Boolean {
            deleteAllFiles += file
            return deleteResult
        }
    }

    private class FakeFileAccess : RuntimeOverrideFileAccess {
        val restoreContextCalls = mutableListOf<Boolean>()

        override fun prepare(
            androidPackageName: String,
            databaseFile: File,
            restoreContext: Boolean,
        ) {
            restoreContextCalls += restoreContext
        }
    }
}
