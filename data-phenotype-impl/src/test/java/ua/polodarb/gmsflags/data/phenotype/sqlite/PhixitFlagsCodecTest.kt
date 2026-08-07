package ua.polodarb.gmsflags.data.phenotype.sqlite

import com.google.protobuf.CodedOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater

class PhixitFlagsCodecTest {
    @Test
    fun `decodes numeric deltas and named values`() {
        val encoded = encode(
            listOf(
                PhixitFlag.Bool("100", true),
                PhixitFlag.Int("105", 42),
                PhixitFlag.StringValue("named_flag", "value"),
            )
        )

        val decoded = PhixitFlagsCodec.decode(encoded)

        assertEquals(PhixitFlag.Bool("100", true), decoded[0])
        assertEquals(PhixitFlag.Int("105", 42), decoded[1])
        assertEquals(PhixitFlag.StringValue("named_flag", "value"), decoded[2])
    }

    @Test
    fun `truncated data fails instead of looping`() {
        assertTrue(runCatching { PhixitFlagsCodec.decode(byteArrayOf(1, 2, 3)) }.isFailure)
    }
}

private fun encode(flags: List<PhixitFlag>): ByteArray {
    val raw = ByteArrayOutputStream()
    val output = CodedOutputStream.newInstance(raw)
    var previousNumericName = 0L
    output.writeUInt32NoTag(flags.size)
    flags.forEach { flag ->
        val type = when (flag) {
            is PhixitFlag.Bool -> if (flag.value) 1L else 0L
            is PhixitFlag.Int -> 2L
            is PhixitFlag.Float -> 3L
            is PhixitFlag.StringValue -> 4L
            is PhixitFlag.Extension -> 5L
        }
        val numericName = flag.name.toLongOrNull()
        if (numericName == null) {
            output.writeUInt64NoTag(type)
            output.writeStringNoTag(flag.name)
        } else {
            output.writeUInt64NoTag(((numericName - previousNumericName) shl 3) or type)
            previousNumericName = numericName
        }
        when (flag) {
            is PhixitFlag.Bool -> Unit
            is PhixitFlag.Int -> output.writeUInt64NoTag(flag.value)
            is PhixitFlag.Float -> output.writeDoubleNoTag(java.lang.Double.longBitsToDouble(flag.value))
            is PhixitFlag.StringValue -> output.writeStringNoTag(flag.value)
            is PhixitFlag.Extension -> output.writeByteArrayNoTag(flag.value)
        }
    }
    output.flush()

    val compressed = ByteArrayOutputStream()
    val deflater = Deflater(1, true).apply { setInput(raw.toByteArray()); finish() }
    val buffer = ByteArray(1024)
    while (!deflater.finished()) {
        compressed.write(buffer, 0, deflater.deflate(buffer))
    }
    deflater.end()
    return compressed.toByteArray()
}
