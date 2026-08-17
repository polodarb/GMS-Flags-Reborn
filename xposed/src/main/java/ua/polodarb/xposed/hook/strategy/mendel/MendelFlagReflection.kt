package ua.polodarb.xposed.hook.strategy.mendel

import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object MendelFlagReflection {
    fun findConfigMapType(candidate: Class<*>): Class<*>? {
        val methods = candidate.declaredMethods
        return methods.asSequence()
            .filter { it.isTypedReader(Boolean::class.javaPrimitiveType) }
            .map { it.parameterTypes[0] }
            .distinct()
            .filter { configMapType ->
                methods.any { it.isTypedReader(Long::class.javaPrimitiveType, configMapType) } &&
                    methods.any { it.isTypedReader(Double::class.javaPrimitiveType, configMapType) }
            }
            .singleOrNull()
    }

    fun findLeafReaders(candidate: Class<*>, configMapType: Class<*>): List<Method> =
        candidate.declaredMethods.filter { it.isLeafReader(configMapType) }

    private fun Method.isLeafReader(configMapType: Class<*>): Boolean {
        if (!Modifier.isStatic(modifiers) ||
            parameterTypes.firstOrNull() != configMapType ||
            parameterTypes.getOrNull(1) != Long::class.javaPrimitiveType
        ) {
            return false
        }
        return when (returnType) {
            Boolean::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
            Double::class.javaPrimitiveType -> isTypedReader(returnType, configMapType)
            String::class.java -> parameterTypes.size in 2..3 &&
                (parameterTypes.size == 2 || parameterTypes[2] == String::class.java)
            else -> false
        }
    }

    private fun Method.isTypedReader(
        valueType: Class<*>?,
        configMapType: Class<*>? = null,
    ): Boolean = Modifier.isStatic(modifiers) &&
        returnType == valueType &&
        parameterTypes.size == 3 &&
        parameterTypes[1] == Long::class.javaPrimitiveType &&
        parameterTypes[2] == valueType &&
        (configMapType == null || parameterTypes[0] == configMapType)
}
