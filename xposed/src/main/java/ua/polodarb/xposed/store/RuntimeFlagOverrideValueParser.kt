package ua.polodarb.xposed.store

internal object RuntimeFlagOverrideValueParser {

    fun parse(
        original: Any?,
        override: RuntimeFlagOverrideStore.Override,
    ): Any? {
        if (!isCompatible(original, override)) return null

        val value = override.value
        return when (original) {
            is Boolean -> value.isStoredTrue()
            is Long -> value.toLongOrNull()
            is Int -> value.toIntOrNull()
            is Double -> value.toDoubleOrNull()
            is Float -> value.toFloatOrNull()
            is String -> value
            else -> parseByStoredType(override)
        }
    }

    private fun isCompatible(
        original: Any?,
        override: RuntimeFlagOverrideStore.Override,
    ): Boolean {
        if (original == null) return true

        return when (override.flagType) {
            TYPE_BOOLEAN -> original is Boolean
            TYPE_INTEGER -> original is Long || original is Int
            TYPE_FLOAT -> original is Double || original is Float
            TYPE_STRING -> original is String
            else -> false
        }
    }

    private fun parseByStoredType(override: RuntimeFlagOverrideStore.Override): Any? {
        return when (override.flagType) {
            TYPE_BOOLEAN -> override.value.isStoredTrue()
            TYPE_INTEGER -> override.value.toLongOrNull()
            TYPE_FLOAT -> override.value.toDoubleOrNull()
            TYPE_STRING -> override.value
            else -> null
        }
    }

    private fun String.isStoredTrue(): Boolean =
        this == STORED_TRUE_LITERAL || equals("true", ignoreCase = true)

    private const val STORED_TRUE_LITERAL = "1"
    private const val TYPE_BOOLEAN = 0
    private const val TYPE_INTEGER = 1
    private const val TYPE_FLOAT = 2
    private const val TYPE_STRING = 3
}
