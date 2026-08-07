package ua.polodarb.xposed.needle

import android.content.res.Resources
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.info.needle.MicroHookSelector
import ua.polodarb.xposed.logging.XposedLogger
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Resolves a [MicroHookSelector] against the real installed target APK, on this device, right
 * now - the same live, on-device DexKit resolution pattern other strategies in this module already
 * use in production/debug today. Fails closed: anything other than exactly one match is treated as
 * "not resolvable" (never guesses).
 */
internal object NeedleSelectorResolver {

    /**
     * Recipe-authored modifier/return-type names are plain server-controlled strings, not a
     * Kotlin enum - matching against named maps here (rather than inline literals) keeps a typo
     * from silently falling through to "match everything" or "no type constraint".
     */
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

    fun resolve(bridge: DexKitBridge, selector: MicroHookSelector, classLoader: ClassLoader): Method? {
        val candidates = bridge.findMethod {
            matcher {
                if (selector.classUsingStringsAll.isNotEmpty()) {
                    declaredClass { usingStrings(selector.classUsingStringsAll) }
                }
                if (selector.methodUsingStringsAll.isNotEmpty()) {
                    usingStrings(selector.methodUsingStringsAll)
                }
                returnType(returnTypeFor(selector.methodReturnType))
            }
        }.filter { methodData ->
            val modifiers = methodData.modifiers
            val isConcrete = (modifiers and Modifier.ABSTRACT) == 0
            val matchesParamCount = selector.methodParameterTypes.isEmpty() ||
                methodData.paramTypes.size == selector.methodParameterTypes.size
            val matchesModifiers = selector.methodModifiersAll.all { required ->
                val flag = MODIFIER_FLAGS[required] ?: return@all true
                (modifiers and flag) != 0
            }
            isConcrete && matchesParamCount && matchesModifiers
        }.distinctBy { it.descriptor }

        if (candidates.size != 1) {
            XposedLogger.logW(
                "NeedleSelectorResolver: expected exactly 1 match, found ${candidates.size} " +
                    "for selector (returnType=${selector.methodReturnType}, strings=${selector.methodUsingStringsAll})",
            )
            return null
        }

        return runCatching { candidates.single().getMethodInstance(classLoader) }
            .onFailure { error -> XposedLogger.logE("NeedleSelectorResolver: failed to bind resolved method", error) }
            .getOrNull()
    }

    /** The single engine-hardcoded framework hook target for
     * [ua.polodarb.xposed.info.needle.SelectorKind.ANDROID_RESOURCE_STRING] recipes. Deliberately
     * not driven by any selector field - a recipe can pick which resource id to react to (via its
     * effect's `when` condition), never which framework class/method gets hooked.
     * `Resources#getString(int)` is not present in a third-party APK's own dex, so unlike
     * [resolve] this does not use DexKit at all - it's a direct JVM reflection lookup against the
     * framework class, available in any classloader. */
    fun resolveResourceGetString(): Method? = runCatching {
        Resources::class.java.getMethod("getString", Int::class.javaPrimitiveType)
    }.onFailure { error ->
        XposedLogger.logE("NeedleSelectorResolver: failed to resolve Resources#getString(int)", error)
    }.getOrNull()

    private fun returnTypeFor(name: String): Class<*> = PRIMITIVE_RETURN_TYPES[name] ?: Any::class.java
}
