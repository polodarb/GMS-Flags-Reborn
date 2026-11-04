package ua.polodarb.gmsflags.data.phenotype.root.hooks

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class HookDiagnosticsReaderTest {
    @Test
    fun `credential-protected storage is checked first, device-protected second`() {
        assertEquals(
            listOf(
                File("/data/user/0/com.example/gmsflags_xposed"),
                File("/data/user_de/0/com.example/gmsflags_xposed"),
            ),
            runtimeDirectoryCandidates("/data/user/0/com.example"),
        )
    }

    @Test
    fun `an unrecognised data directory yields itself only`() {
        assertEquals(
            listOf(File("/mnt/custom/com.example/gmsflags_xposed")),
            runtimeDirectoryCandidates("/mnt/custom/com.example"),
        )
    }
}
