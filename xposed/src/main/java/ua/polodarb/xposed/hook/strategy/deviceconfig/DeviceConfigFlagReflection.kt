package ua.polodarb.xposed.hook.strategy.deviceconfig

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object DeviceConfigFlagReflection {
    fun findRawReaderMethod(managerClass: Class<*>): Method? =
        managerClass.declaredMethods.filter { method ->
            !Modifier.isStatic(method.modifiers) &&
                !Modifier.isAbstract(method.modifiers) &&
                method.returnType == String::class.java &&
                method.parameterTypes.contentEquals(arrayOf(String::class.java))
        }.singleOrNull()

    fun readNamespace(manager: Any): String? =
        generateSequence(manager.javaClass) { current -> current.superclass }
            .takeWhile { current -> current != Any::class.java }
            .flatMap { current -> current.declaredFields.asSequence() }
            .filter { field ->
                !Modifier.isStatic(field.modifiers) &&
                    field.type == String::class.java
            }
            .mapNotNull { field -> field.readString(manager) }
            .filter { value -> value.isNotBlank() }
            .distinct()
            .singleOrNull()

    private fun Field.readString(instance: Any): String? = runCatching {
        isAccessible = true
        get(instance) as? String
    }.getOrNull()
}
