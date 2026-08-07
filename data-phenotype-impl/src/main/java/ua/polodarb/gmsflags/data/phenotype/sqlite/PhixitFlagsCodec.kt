package ua.polodarb.gmsflags.data.phenotype.sqlite

import com.google.protobuf.CodedInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

internal object PhixitFlagsCodec {
    fun decode(compressedData: ByteArray): List<PhixitFlag> {
        val input = CodedInputStream.newInstance(decompress(compressedData))
        val size = input.readUInt32()
        var previousNumericName = 0L

        return List(size) {
            val descriptor = input.readUInt64()
            val nameDelta = descriptor ushr 3
            val name = if (nameDelta == 0L) {
                input.readString()
            } else {
                (previousNumericName + nameDelta).also { previousNumericName = it }.toString()
            }

            when (val type = descriptor.toInt() and 7) {
                0, 1 -> PhixitFlag.Bool(name, type == 1)
                2 -> PhixitFlag.Int(name, input.readUInt64())
                3 -> PhixitFlag.Float(name, java.lang.Double.doubleToRawLongBits(input.readDouble()))
                4 -> PhixitFlag.StringValue(name, input.readString())
                5 -> PhixitFlag.Extension(name, input.readByteArray())
                else -> error("Unknown Phixit flag type: $type")
            }
        }
    }

    private fun decompress(data: ByteArray): ByteArray {
        val inflater = Inflater(true).apply { setInput(data) }
        return try {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0) {
                    require(!inflater.needsDictionary()) { "Phixit data requires a dictionary" }
                    require(!inflater.needsInput()) { "Truncated Phixit data" }
                    error("Unable to inflate Phixit data")
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } finally {
            inflater.end()
        }
    }
}

internal sealed interface PhixitFlag {
    val name: String

    data class Bool(override val name: String, val value: Boolean) : PhixitFlag
    data class Int(override val name: String, val value: Long) : PhixitFlag
    data class Float(override val name: String, val value: Long) : PhixitFlag
    data class StringValue(override val name: String, val value: String) : PhixitFlag
    data class Extension(override val name: String, val value: ByteArray) : PhixitFlag
}
