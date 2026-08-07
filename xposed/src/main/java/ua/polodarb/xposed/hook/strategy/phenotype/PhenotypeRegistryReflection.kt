package ua.polodarb.xposed.hook.strategy.phenotype

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object PhenotypeRegistryReflection {
    fun findLookupMethod(registryClass: Class<*>): Method? =
        registryClass.declaredMethods.filter(::isLookupMethod).singleOrNull()

    fun findBoundLookupMethod(registryClass: Class<*>): Method? =
        registryClass.declaredMethods.filter(::isBoundLookupMethod).singleOrNull()

    fun findValueConstructor(valueClass: Class<*>): ValueConstructor? {
        val constructors = valueClass.declaredConstructors
        val integerTypeConstructor = constructors.filter { constructor ->
            val types = constructor.parameterTypes
            types.size == INTEGER_TYPE_CONSTRUCTOR_PARAMETER_COUNT &&
                types[SUPPLIER_PARAMETER_INDEX].isInterface &&
                types[TYPE_PARAMETER_INDEX] == Int::class.javaPrimitiveType &&
                types[INTEGER_TYPE_METADATA_PARAMETER_INDEX] == Boolean::class.javaPrimitiveType
        }.singleOrNull()
        if (integerTypeConstructor != null) {
            return ValueConstructor(integerTypeConstructor, TypeEncoding.Integer)
        }

        val enumTypeConstructor = constructors.filter { constructor ->
            val types = constructor.parameterTypes
            types.size == ENUM_TYPE_CONSTRUCTOR_PARAMETER_COUNT &&
                types[SUPPLIER_PARAMETER_INDEX].isInterface &&
                types[TYPE_PARAMETER_INDEX].isEnum &&
                types[ENUM_TYPE_METADATA_PARAMETER_INDEX] == Boolean::class.javaPrimitiveType
        }.singleOrNull()
        if (enumTypeConstructor != null) {
            return ValueConstructor(enumTypeConstructor, TypeEncoding.Enum)
        }

        val taggedIntegerConstructor = constructors.filter { constructor ->
            val types = constructor.parameterTypes
            types.size == TAGGED_INTEGER_CONSTRUCTOR_PARAMETER_COUNT &&
                types[SUPPLIER_PARAMETER_INDEX].isInterface &&
                types[TYPE_PARAMETER_INDEX] == Int::class.javaPrimitiveType &&
                !types[TAGGED_INTEGER_METADATA_PARAMETER_INDEX].isPrimitive
        }.singleOrNull()
        return taggedIntegerConstructor?.let { constructor ->
            ValueConstructor(constructor, TypeEncoding.TaggedInteger)
        }
    }

    fun findSupplierMethod(supplierType: Class<*>): Method? = supplierType.methods
        .filter { method ->
            Modifier.isAbstract(method.modifiers) &&
                method.parameterCount == 0 &&
                method.returnType != Void.TYPE
        }
        .singleOrNull()

    fun readBooleanMetadata(value: Any): Boolean = findBooleanMetadataField(value.javaClass)
        ?.let { field ->
            runCatching {
                field.isAccessible = true
                field.getBoolean(value)
            }.getOrNull()
        }
        ?: false

    fun createValue(
        valueConstructor: ValueConstructor,
        supplier: Any,
        flagType: Int,
        booleanMetadata: Boolean,
    ): Any? {
        val constructor = valueConstructor.constructor.apply { isAccessible = true }
        return when (valueConstructor.typeEncoding) {
            TypeEncoding.Integer -> constructor.newInstance(
                supplier,
                integerTypeFor(flagType) ?: return null,
                null,
                booleanMetadata,
            )

            TypeEncoding.Enum -> constructor.newInstance(
                supplier,
                enumTypeFor(constructor.parameterTypes[TYPE_PARAMETER_INDEX], flagType)
                    ?: return null,
                booleanMetadata,
            )

            TypeEncoding.TaggedInteger -> constructor.newInstance(
                supplier,
                integerTypeFor(flagType) ?: return null,
                null,
            )
        }
    }

    private fun isLookupMethod(method: Method): Boolean {
        if (Modifier.isStatic(method.modifiers)) return false
        if (!method.parameterTypes.contentEquals(LOOKUP_PARAMETER_TYPES)) return false
        return findValueConstructor(method.returnType) != null
    }

    private fun isBoundLookupMethod(method: Method): Boolean {
        if (Modifier.isStatic(method.modifiers)) return false
        if (!method.parameterTypes.contentEquals(BOUND_LOOKUP_PARAMETER_TYPES)) return false
        return findValueConstructor(method.returnType) != null
    }

    private fun findBooleanMetadataField(valueClass: Class<*>): Field? {
        val candidates = generateSequence(valueClass) { current -> current.superclass }
            .flatMap { current -> current.declaredFields.asSequence() }
            .filter { field ->
                !Modifier.isStatic(field.modifiers) &&
                    field.type == Boolean::class.javaPrimitiveType
            }
            .toList()
        return candidates.singleOrNull()
    }

    private fun integerTypeFor(flagType: Int): Int? = when (flagType) {
        TYPE_BOOLEAN -> 2
        TYPE_INTEGER -> 1
        TYPE_FLOAT -> 3
        TYPE_STRING -> 4
        else -> null
    }

    private fun enumTypeFor(enumClass: Class<*>, flagType: Int): Any? {
        val (constantName, fallbackOrdinal) = when (flagType) {
            TYPE_BOOLEAN -> "BOOLEAN_VALUE" to 1
            TYPE_INTEGER -> "LONG_VALUE" to 0
            TYPE_FLOAT -> "DOUBLE_VALUE" to 2
            TYPE_STRING -> "STRING_VALUE" to 3
            else -> return null
        }
        val constants = enumClass.enumConstants ?: return null
        return constants.firstOrNull { constant ->
            (constant as? Enum<*>)?.name == constantName
        } ?: constants.getOrNull(fallbackOrdinal)
    }

    private val LOOKUP_PARAMETER_TYPES = arrayOf(String::class.java, String::class.java)
    private val BOUND_LOOKUP_PARAMETER_TYPES = arrayOf(String::class.java)

    private const val INTEGER_TYPE_CONSTRUCTOR_PARAMETER_COUNT = 4
    private const val ENUM_TYPE_CONSTRUCTOR_PARAMETER_COUNT = 3
    private const val TAGGED_INTEGER_CONSTRUCTOR_PARAMETER_COUNT = 3
    private const val SUPPLIER_PARAMETER_INDEX = 0
    private const val TYPE_PARAMETER_INDEX = 1
    private const val INTEGER_TYPE_METADATA_PARAMETER_INDEX = 3
    private const val ENUM_TYPE_METADATA_PARAMETER_INDEX = 2
    private const val TAGGED_INTEGER_METADATA_PARAMETER_INDEX = 2

    private const val TYPE_BOOLEAN = 0
    private const val TYPE_INTEGER = 1
    private const val TYPE_FLOAT = 2
    private const val TYPE_STRING = 3

    internal data class ValueConstructor(
        val constructor: Constructor<*>,
        val typeEncoding: TypeEncoding,
    )

    internal enum class TypeEncoding {
        Integer,
        Enum,
        TaggedInteger,
    }
}
