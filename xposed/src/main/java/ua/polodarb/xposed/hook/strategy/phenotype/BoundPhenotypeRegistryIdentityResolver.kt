package ua.polodarb.xposed.hook.strategy.phenotype

import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object BoundPhenotypeRegistryIdentityResolver {
    fun findPackageName(receiver: Any): String? {
        val registry = receiver.instanceFields()
            .mapNotNull { field -> field.read(receiver) }
            .singleOrNull()
            ?: return null

        return registry.instanceFields()
            .mapNotNull { field -> field.read(registry) as? Map<*, *> }
            .mapNotNull(::singleStringKey)
            .distinct()
            .singleOrNull()
    }

    private fun singleStringKey(map: Map<*, *>): String? {
        if (map.isEmpty() || map.keys.any { it !is String }) return null
        return map.keys.filterIsInstance<String>()
            .filter { it.isNotBlank() }
            .distinct()
            .singleOrNull()
    }

    private fun Any.instanceFields(): Sequence<Field> =
        generateSequence(javaClass) { current -> current.superclass }
            .takeWhile { current -> current != Any::class.java }
            .flatMap { current -> current.declaredFields.asSequence() }
            .filter { field -> !Modifier.isStatic(field.modifiers) }

    private fun Field.read(instance: Any): Any? = runCatching {
        isAccessible = true
        get(instance)
    }.getOrNull()
}
