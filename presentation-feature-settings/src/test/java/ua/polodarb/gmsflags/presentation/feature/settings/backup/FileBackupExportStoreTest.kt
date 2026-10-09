package ua.polodarb.gmsflags.presentation.feature.settings.backup

import java.nio.file.Files
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileBackupExportStoreTest {
    @Test
    fun `prepared exports survive store recreation and are deleted after use`() = runTest {
        val directory = Files.createTempDirectory("backup-exports").toFile()
        try {
            val id = FileBackupExportStore(directory).prepare("<backup>🦊</backup>")
            val recreated = FileBackupExportStore(directory)
            assertEquals("<backup>🦊</backup>", recreated.read(id))
            recreated.delete(id)
            assertTrue(directory.listFiles().orEmpty().isEmpty())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun `identifiers cannot read files outside the export directory`() = runTest {
        val directory = Files.createTempDirectory("backup-exports").toFile()
        try {
            assertTrue(
                runCatching { FileBackupExportStore(directory).read("../outside") }.isFailure
            )
            assertFalse(directory.resolve("outside").exists())
        } finally {
            directory.deleteRecursively()
        }
    }
}
