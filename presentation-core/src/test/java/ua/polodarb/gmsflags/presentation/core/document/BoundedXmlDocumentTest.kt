package ua.polodarb.gmsflags.presentation.core.document

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundedXmlDocumentTest {
    @Test
    fun `limits UTF-8 bytes rather than characters`() {
        assertEquals("🦊", "🦊".byteInputStream().readBoundedUtf8(4))
        assertTrue(runCatching { "🦊".byteInputStream().readBoundedUtf8(3) }.isFailure)
    }

    @Test
    fun `rejects malformed UTF-8`() {
        assertTrue(
            runCatching { byteArrayOf(0xc3.toByte(), 0x28).inputStream().readBoundedUtf8(10) }
                .isFailure
        )
    }
}
