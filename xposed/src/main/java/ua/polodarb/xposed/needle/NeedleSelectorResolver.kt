package ua.polodarb.xposed.needle

import android.content.res.Resources
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.query.matchers.MethodMatcher
import org.luckypray.dexkit.result.MethodData
import ua.polodarb.xposed.info.needle.MicroHookSelector
import ua.polodarb.xposed.info.needle.NeedleModifierNames
import ua.polodarb.xposed.info.needle.NeedleRecipePayload
import ua.polodarb.xposed.logging.XposedLogger
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal sealed interface NeedleResolution {
    data class Resolved(val method: Method) : NeedleResolution
    data object NoMatch : NeedleResolution
    data class Ambiguous(val count: Int, val descriptors: List<String>) : NeedleResolution
    data class InvalidSelector(val reason: String) : NeedleResolution
    data class BindFailed(val reason: String) : NeedleResolution
}

internal object NeedleSelectorResolver {

    private const val SCHEMA_VERSION_V2 = 2

    private val MODIFIER_FLAGS: Map<String, Int> = mapOf(
        "public" to Modifier.PUBLIC,
        "private" to Modifier.PRIVATE,
        "protected" to Modifier.PROTECTED,
        "static" to Modifier.STATIC,
        "final" to Modifier.FINAL,
    )

    private val PRIMITIVE_RETURN_TYPES: Map<String, Class<*>> = mapOf(
        "boolean" to Boolean::class.javaPrimitiveType!!,
        "int" to Int::class.javaPrimitiveType!!,
        "long" to Long::class.javaPrimitiveType!!,
        "float" to Float::class.javaPrimitiveType!!,
        "double" to Double::class.javaPrimitiveType!!,
        "void" to Void.TYPE,
    )

    fun resolve(
        bridge: DexKitBridge,
        payload: NeedleRecipePayload,
        classLoader: ClassLoader,
    ): NeedleResolution {
        val selector = payload.selector
        val isV2 = payload.schemaVersion >= SCHEMA_VERSION_V2

        if (isV2) {
            selector.methodModifiersAll.firstOrNull { !NeedleModifierNames.isKnown(it) }?.let { unknown ->
                return NeedleResolution.InvalidSelector("unknown method modifier '$unknown'")
            }
            selector.classHasFieldsAll.flatMap { it.modifiersAll }
                .firstOrNull { !NeedleModifierNames.isKnown(it) }
                ?.let { unknown -> return NeedleResolution.InvalidSelector("unknown field modifier '$unknown'") }
        }

        val candidates = queryVariants(selector, isV2)
            .flatMap { variant -> runQuery(bridge, selector, variant, isV2) }
            .filter { methodData -> matchesPostFilters(methodData, selector, isV2) }
            .distinctBy { it.descriptor }

        if (candidates.isEmpty()) return NeedleResolution.NoMatch
        if (candidates.size > 1) {
            return NeedleResolution.Ambiguous(candidates.size, candidates.map { it.descriptor })
        }

        return runCatching { candidates.single().getMethodInstance(classLoader) }
            .fold(
                onSuccess = { NeedleResolution.Resolved(it) },
                onFailure = { error ->
                    NeedleResolution.BindFailed(error.message ?: error.javaClass.simpleName)
                },
            )
    }

    private data class QueryVariant(val classAnyString: String?, val methodAnyString: String?)

    private fun queryVariants(selector: MicroHookSelector, isV2: Boolean): List<QueryVariant> {
        if (!isV2) return listOf(QueryVariant(null, null))
        val classAlternatives = selector.classUsingStringsAny.ifEmpty { listOf(null) }
        val methodAlternatives = selector.methodUsingStringsAny.ifEmpty { listOf(null) }
        return classAlternatives.flatMap { classAny ->
            methodAlternatives.map { methodAny -> QueryVariant(classAny, methodAny) }
        }
    }

    private fun runQuery(
        bridge: DexKitBridge,
        selector: MicroHookSelector,
        variant: QueryVariant,
        isV2: Boolean,
    ): List<MethodData> = runCatching {
        bridge.findMethod {
            matcher {
                buildClassMatcher(selector, variant, isV2, this)

                val methodStrings = selector.methodUsingStringsAll + listOfNotNull(variant.methodAnyString)
                if (methodStrings.isNotEmpty()) {
                    if (isV2) usingStrings(methodStrings, StringMatchType.Equals) else usingStrings(methodStrings)
                }

                if (isV2) {
                    returnType(selector.methodReturnType, StringMatchType.Equals)
                    paramTypes(selector.methodParameterTypes)
                    selector.methodInvokesAll.forEach { invoked ->
                        addInvoke(
                            MethodMatcher()
                                .declaredClass(invoked.declaringType, StringMatchType.Equals)
                                .name(invoked.name)
                                .returnType(invoked.returnType, StringMatchType.Equals)
                                .paramTypes(invoked.parameterTypes),
                        )
                    }
                } else {
                    returnType(legacyReturnTypeFor(selector.methodReturnType))
                }
            }
        }.toList()
    }.getOrElse { error ->
        XposedLogger.logE("NeedleSelectorResolver: dex query failed", error)
        emptyList()
    }

    private fun buildClassMatcher(
        selector: MicroHookSelector,
        variant: QueryVariant,
        isV2: Boolean,
        matcher: MethodMatcher,
    ) {
        val classStrings = selector.classUsingStringsAll + listOfNotNull(variant.classAnyString)
        val hasClassConstraint = classStrings.isNotEmpty() ||
            (isV2 && (selector.classHasMethodsAll.isNotEmpty() || selector.classHasFieldsAll.isNotEmpty()))
        if (!hasClassConstraint) return

        matcher.declaredClass {
            if (classStrings.isNotEmpty()) {
                if (isV2) usingStrings(classStrings, StringMatchType.Equals) else usingStrings(classStrings)
            }
            if (!isV2) return@declaredClass
            selector.classHasMethodsAll.forEach { signature ->
                addMethod {
                    returnType(signature.returnType, StringMatchType.Equals)
                    paramTypes(signature.parameterTypes)
                }
            }
            selector.classHasFieldsAll.forEach { signature ->
                addField {
                    type(signature.type, StringMatchType.Equals)
                    modifierMaskOf(signature.modifiersAll)?.let { modifiers(it) }
                }
            }
        }
    }

    private fun matchesPostFilters(
        methodData: MethodData,
        selector: MicroHookSelector,
        isV2: Boolean,
    ): Boolean {
        val modifiers = methodData.modifiers
        if ((modifiers and Modifier.ABSTRACT) != 0) return false

        val matchesModifiers = selector.methodModifiersAll.all { required ->
            val flag = MODIFIER_FLAGS[required] ?: return@all !isV2
            (modifiers and flag) != 0
        }
        if (!matchesModifiers) return false

        if (isV2) return true

        return selector.methodParameterTypes.isEmpty() ||
            methodData.paramTypes.size == selector.methodParameterTypes.size
    }

    private fun modifierMaskOf(names: List<String>): Int? {
        if (names.isEmpty()) return null
        var mask = 0
        names.forEach { name -> mask = mask or (MODIFIER_FLAGS[name] ?: return null) }
        return mask
    }

    fun resolveResourceGetString(): Method? = runCatching {
        Resources::class.java.getMethod("getString", Int::class.javaPrimitiveType)
    }.onFailure { error ->
        XposedLogger.logE("NeedleSelectorResolver: failed to resolve Resources#getString(int)", error)
    }.getOrNull()

    private fun legacyReturnTypeFor(name: String): Class<*> = PRIMITIVE_RETURN_TYPES[name] ?: Any::class.java
}
