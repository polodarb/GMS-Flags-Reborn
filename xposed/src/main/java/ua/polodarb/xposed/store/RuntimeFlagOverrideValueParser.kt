package ua.polodarb.xposed.store

internal object RuntimeFlagOverrideValueParser {

    fun parse(
        original: Any?,
        override: RuntimeFlagOverrideStore.Override,
        declaredType: Class<*>? = null,
    ): Any? {
        if (declaredType != null && !matchesDeclaredType(declaredType, override.flagType)) return null
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

    private fun matchesDeclaredType(declaredType: Class<*>, flagType: Int): Boolean =
        when (flagType) {
            TYPE_BOOLEAN -> declaredType.isOneOf(Boolean::class)
            TYPE_INTEGER -> declaredType.isOneOf(Long::class) || declaredType.isOneOf(Int::class)
            TYPE_FLOAT -> declaredType.isOneOf(Double::class) || declaredType.isOneOf(Float::class)
            TYPE_STRING -> declaredType == String::class.java
            else -> false
        }

    private fun Class<*>.isOneOf(type: kotlin.reflect.KClass<*>): Boolean =
        this == type.javaPrimitiveType || this == type.javaObjectType

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
