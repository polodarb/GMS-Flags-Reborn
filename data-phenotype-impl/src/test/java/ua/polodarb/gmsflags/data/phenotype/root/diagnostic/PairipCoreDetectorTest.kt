package ua.polodarb.gmsflags.data.phenotype.root.diagnostic

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PairipCoreDetectorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val detector = PairipCoreDetector()

    @Test
    fun `detects an extracted PairIP library with one file check`() {
        val nativeDirectory = temporaryFolder.newFolder("native")
        File(nativeDirectory, "libpairipcore.so").writeBytes(byteArrayOf(1))

        assertTrue(
            detector.isPresent(
                apkFiles = emptyList(),
                nativeLibraryDirectory = nativeDirectory,
                nativeLibrariesExtracted = true,
            )
        )
    }

    @Test
    fun `detects an embedded PairIP library in a split APK`() {
        val baseApk = createApk("base.apk", "lib/arm64-v8a/libother.so")
        val splitApk = createApk(
            "split_config.arm64_v8a.apk",
            "lib/arm64-v8a/libpairipcore.so",
        )

        assertTrue(
            detector.isPresent(
                apkFiles = listOf(baseApk, splitApk),
                nativeLibraryDirectory = null,
                nativeLibrariesExtracted = false,
            )
        )
    }

    @Test
    fun `does not scan APKs when PackageManager says libraries are extracted`() {
        val apk = createApk("base.apk", "lib/arm64-v8a/libpairipcore.so")

        assertFalse(
            detector.isPresent(
                apkFiles = listOf(apk),
                nativeLibraryDirectory = temporaryFolder.newFolder("empty-native"),
                nativeLibrariesExtracted = true,
            )
        )
    }

    @Test
    fun `ignores unrelated native libraries`() {
        val apk = createApk("base.apk", "lib/arm64-v8a/libnotpairipcore.so")

        assertFalse(
            detector.isPresent(
                apkFiles = listOf(apk),
                nativeLibraryDirectory = null,
                nativeLibrariesExtracted = false,
            )
        )
    }

    private fun createApk(name: String, vararg entries: String): File {
        val file = temporaryFolder.newFile(name)
        ZipOutputStream(file.outputStream()).use { archive ->
            entries.forEach { entryName ->
                archive.putNextEntry(ZipEntry(entryName))
                archive.write(byteArrayOf(1))
                archive.closeEntry()
            }
        }
        return file
    }
}
