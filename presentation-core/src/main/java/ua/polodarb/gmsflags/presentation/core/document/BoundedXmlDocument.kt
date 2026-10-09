package ua.polodarb.gmsflags.presentation.core.document

import java.io.FilterInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.CodingErrorAction

fun InputStream.readBoundedUtf8(maxBytes: Int): String {
    val limited = object : FilterInputStream(this) {
        private var bytes = 0L

        private fun counted(count: Int): Int {
            if (count > 0) {
                bytes += count
            }
            require(bytes <= maxBytes) { "Import document is too large" }
            return count
        }

        override fun read(): Int = super.read().also {
            if (it >= 0) {
                counted(1)
            }
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            counted(super.read(buffer, offset, length))
    }
    val decoder = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
    return InputStreamReader(limited, decoder).use { it.readText() }
}
