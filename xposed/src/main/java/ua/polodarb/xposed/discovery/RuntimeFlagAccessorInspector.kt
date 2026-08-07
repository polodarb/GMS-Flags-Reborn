package ua.polodarb.xposed.discovery

import android.net.Uri
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object RuntimeFlagAccessorInspector {

    fun findPhenotypeIdentity(accessor: Any, contextPackageName: String): RuntimeFlagIdentity? {
        val hierarchy = accessor.classHierarchy()

        for (clazz in hierarchy.drop(1)) {
            resolvePhenotypeIdentity(clazz.readStringFields(accessor), contextPackageName)?.let { return it }
        }

        val fallbackStrings = hierarchy.asReversed().flatMap { clazz ->
            clazz.readStringFields(accessor)
        }
        resolvePhenotypeIdentity(fallbackStrings, contextPackageName)?.let { return it }

        resolveNestedDescriptorIdentity(accessor, hierarchy)?.let { return it }

        return resolvePrefixedFlagIdentity(
            accessor = accessor,
            hierarchy = hierarchy,
            contextPackageName = contextPackageName,
        )
    }

    fun describeStringFields(accessor: Any): String {
        val values = accessor.classHierarchy()
            .flatMap { clazz ->
                clazz.declaredFields
                    .asSequence()
                    .filter { field -> field.isInstanceStringField() }
                    .mapNotNull { field ->
                        field.readString(accessor)?.let { value -> "${clazz.simpleName}.${field.name}=$value" }
                    }
            }
            .toList()

        if (values.isEmpty()) return "[]"
        return values.take(MAX_LOG_VALUES)
            .joinToString(prefix = "[", postfix = if (values.size > MAX_LOG_VALUES) ", ...]" else "]") {
                it.take(MAX_LOG_VALUE_LENGTH)
            }
    }

    private fun resolvePhenotypeIdentity(
        strings: List<String>,
        contextPackageName: String,
    ): RuntimeFlagIdentity? {
        val values = strings.distinct().filter { it.isNotBlank() }
        if (values.size < 2) return null

        val packageName = values.firstOrNull(::looksLikePackageName)
            ?: values.firstOrNull { it == contextPackageName }
            ?: return null

        val flagName = values.firstOrNull { value ->
            value != packageName && (value.contains("__") || !looksLikePackageName(value))
        } ?: values.firstOrNull { value -> value != packageName }
            ?: return null

        return RuntimeFlagIdentity(packageName, flagName)
    }

    private fun resolveNestedDescriptorIdentity(
        accessor: Any,
        hierarchy: List<Class<*>>,
    ): RuntimeFlagIdentity? {
        val localFlagName = hierarchy
            .flatMap { clazz -> clazz.readStringFields(accessor) }
            .filter { it.isNotBlank() }
            .distinct()
            .singleOrNull()
            ?: return null

        val identities = hierarchy
            .flatMap { clazz ->
                clazz.declaredFields
                    .asSequence()
                    .filter { field -> field.isInstanceObjectField() }
                    .mapNotNull { field -> field.readValue(accessor) }
                    .toList()
            }
            .distinctBy(System::identityHashCode)
            .mapNotNull { descriptor ->
                resolveDescriptorIdentity(descriptor, localFlagName)
            }
            .distinct()

        return identities.singleOrNull()
    }

    private fun resolveDescriptorIdentity(
        descriptor: Any,
        localFlagName: String,
    ): RuntimeFlagIdentity? {
        val descriptorFields = descriptor.javaClass.declaredFields
            .asSequence()
            .filter { field -> !Modifier.isStatic(field.modifiers) }
            .toList()
        val stringFields = descriptorFields.filter { field -> field.type == String::class.java }
        val uriFields = descriptorFields.filter { field -> field.type == Uri::class.java }
        val booleanFieldCount = descriptorFields.count { field -> field.type == Boolean::class.javaPrimitiveType }

        if (
            stringFields.size != LEGACY_DESCRIPTOR_STRING_FIELD_COUNT ||
            uriFields.size != LEGACY_DESCRIPTOR_URI_FIELD_COUNT ||
            booleanFieldCount < LEGACY_DESCRIPTOR_MIN_BOOLEAN_FIELD_COUNT
        ) {
            return null
        }

        val strings = stringFields.mapNotNull { field -> field.readString(descriptor) }
        val uriPackages = uriFields.mapNotNull { field ->
            (field.readValue(descriptor) as? Uri)?.lastPathSegment
        }
        val packageName = (strings.filter(::looksLikePackageName) + uriPackages)
            .filter { it.isNotBlank() }
            .distinct()
            .singleOrNull()
            ?: return null

        val nonEmptyPrefixes = strings
            .filter { value -> value != packageName && value.isNotBlank() }
            .distinct()
        if (nonEmptyPrefixes.isNotEmpty()) return null

        return RuntimeFlagIdentity(packageName, localFlagName)
    }

    private fun resolvePrefixedFlagIdentity(
        accessor: Any,
        hierarchy: List<Class<*>>,
        contextPackageName: String,
    ): RuntimeFlagIdentity? {
        val (declaringClass, localFlagName) = hierarchy.mapNotNull { clazz ->
            clazz.readStringFields(accessor)
                .singleOrNull()
                ?.let { flagName -> clazz to flagName }
        }.singleOrNull() ?: return null

        val fullFlagNames = declaringClass.declaredMethods
            .asSequence()
            .filter { method ->
                !Modifier.isStatic(method.modifiers) &&
                    !Modifier.isAbstract(method.modifiers) &&
                    method.parameterCount == 0 &&
                    method.returnType == String::class.java
            }
            .mapNotNull { method ->
                runCatching {
                    method.isAccessible = true
                    method.invoke(accessor) as? String
                }.getOrNull()
            }
            .filter { candidate ->
                candidate.isNotBlank() && candidate.endsWith(localFlagName)
            }
            .distinct()
            .toList()

        val fullFlagName = fullFlagNames.singleOrNull() ?: return null
        return RuntimeFlagIdentity(contextPackageName, fullFlagName)
    }

    private fun Any.classHierarchy(): List<Class<*>> {
        return generateSequence(javaClass) { it.superclass }
            .takeWhile { it != Any::class.java }
            .toList()
    }

    private fun Class<*>.readStringFields(instance: Any): List<String> {
        return declaredFields
            .asSequence()
            .filter { field -> field.isInstanceStringField() }
            .mapNotNull { field -> field.readString(instance) }
            .toList()
    }

    private fun Field.isInstanceStringField(): Boolean {
        return !Modifier.isStatic(modifiers) && type == String::class.java
    }

    private fun Field.isInstanceObjectField(): Boolean {
        return !Modifier.isStatic(modifiers) &&
            !type.isPrimitive &&
            type != String::class.java
    }

    private fun Field.readString(instance: Any): String? {
        return readValue(instance) as? String
    }

    private fun Field.readValue(instance: Any): Any? {
        return runCatching {
            isAccessible = true
            get(instance)
        }.getOrNull()
    }

    private fun looksLikePackageName(value: String): Boolean {
        if (value.contains("__")) return false
        return value.matches(DOTTED_CONFIG_PACKAGE) || value.matches(UNDERSCORED_CONFIG_PACKAGE)
    }

    private const val MAX_LOG_VALUES = 12
    private const val MAX_LOG_VALUE_LENGTH = 180
    private const val LEGACY_DESCRIPTOR_STRING_FIELD_COUNT = 3
    private const val LEGACY_DESCRIPTOR_URI_FIELD_COUNT = 1
    private const val LEGACY_DESCRIPTOR_MIN_BOOLEAN_FIELD_COUNT = 3
    private val DOTTED_CONFIG_PACKAGE = Regex("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)+.*")
    private val UNDERSCORED_CONFIG_PACKAGE = Regex("[a-z][a-z0-9]*(?:_[a-z0-9]+)+")
}
