package ua.polodarb.xposed.hook.strategy.vending

import java.lang.reflect.Modifier
import ua.polodarb.xposed.info.XposedTargets

internal object FinskyExperimentPackageResolver {
    fun resolve(
        accessor: Any,
        readerClass: Class<*>,
        flagName: String,
    ): String? {
        if (!readerClass.isInstance(accessor)) return null

        val defaultMaps = readerClass.declaredFields
            .asSequence()
            .filter { field ->
                !Modifier.isStatic(field.modifiers) &&
                    Map::class.java.isAssignableFrom(field.type)
            }
            .mapNotNull { field ->
                runCatching {
                    field.isAccessible = true
                    field.get(accessor) as? Map<*, *>
                }.getOrNull()
            }
            .take(EXPECTED_DEFAULT_MAP_COUNT)
            .toList()

        return resolve(defaultMaps, flagName)
    }

    fun resolve(defaultMaps: List<Map<*, *>>, flagName: String): String? = when {
        defaultMaps.size < EXPECTED_DEFAULT_MAP_COUNT -> null
        defaultMaps[REGULAR_DEFAULTS_INDEX].containsKey(flagName) ->
            XposedTargets.VENDING_REGULAR_FLAGS_PACKAGE_NAME
        defaultMaps[STABLE_DEFAULTS_INDEX].containsKey(flagName) ->
            XposedTargets.VENDING_STABLE_FLAGS_PACKAGE_NAME
        else -> null
    }

    private const val EXPECTED_DEFAULT_MAP_COUNT = 2
    private const val REGULAR_DEFAULTS_INDEX = 0
    private const val STABLE_DEFAULTS_INDEX = 1
}
