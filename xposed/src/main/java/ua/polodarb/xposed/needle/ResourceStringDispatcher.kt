package ua.polodarb.xposed.needle

import android.content.res.Resources
import de.robv.android.xposed.XC_MethodHook
import ua.polodarb.xposed.info.needle.NeedleRecipePayload
import ua.polodarb.xposed.info.needle.ResourceStringConflictResolver
import ua.polodarb.xposed.info.needle.ResourceStringDispatchResult
import ua.polodarb.xposed.info.needle.dispatchKeyResourceSources
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import java.util.WeakHashMap

internal class ResourceStringDispatcher(
    private val candidates: List<NeedleRecipePayload>,
    private val contextPackageName: String,
    private val overrideStore: RuntimeFlagOverrideStore,
) : XC_MethodHook() {
    private val indexCache = WeakHashMap<Resources, Map<Int, List<NeedleRecipePayload>>>()

    override fun afterHookedMethod(param: MethodHookParam) {
        val resources = param.thisObject as? Resources ?: return
        val id = param.args?.getOrNull(0) as? Int ?: return

        val index = synchronized(indexCache) { indexCache.getOrPut(resources) { buildIndex(resources) } }
        val candidatesForThisId = index[id] ?: return

        val context = NeedleHookContext(param, contextPackageName, overrideStore, appContext = null)
        when (val result = ResourceStringConflictResolver.resolve(candidatesForThisId, context)) {
            ResourceStringDispatchResult.NoMatch -> Unit
            is ResourceStringDispatchResult.Applied -> {
                param.result = result.value
                if (result.redundantMatchCount > 0) {
                    XposedLogger.logW(
                        "ResourceStringDispatcher: ${result.redundantMatchCount} redundant recipe(s) " +
                            "also matched resource id $id with the same replacement value",
                    )
                }
            }
            is ResourceStringDispatchResult.Conflict -> XposedLogger.logW(
                "ResourceStringDispatcher: ${result.matches.size} recipes " +
                    "(${result.matches.joinToString { it.recipeId }}) matched resource id $id " +
                    "with ${result.matches.map { it.value }.distinct().size} different replacement values; leaving original text",
            )
        }
    }

    /** Resolves every candidate's target resource id(s) against this [resources] instance ONCE,
     * grouping candidates by resolved id. Resolves names directly via `Resources#getIdentifier`
     * rather than through [NeedleHookContext] since there's no live hook `param` yet at this
     * point. Both the resolved ids and the candidate list per id are deduplicated, since a `when`
     * can reference the same RESOURCE_ID more than once - without dedup [ResourceStringConflictResolver]
     * would see the same recipe twice and misreport it as a "redundant match" against itself. */
    private fun buildIndex(resources: Resources): Map<Int, List<NeedleRecipePayload>> {
        val index = mutableMapOf<Int, MutableList<NeedleRecipePayload>>()
        candidates.forEach { payload ->
            val condition = payload.effect.`when` ?: return@forEach
            val resolvedIds = condition.dispatchKeyResourceSources().mapNotNull { source ->
                runCatching {
                    resources.getIdentifier(source.resourceName, source.resourceType, source.packageName)
                }.getOrDefault(0).takeIf { it != 0 }
            }.distinct()
            resolvedIds.forEach { id -> index.getOrPut(id) { mutableListOf() }.add(payload) }
        }
        return index.mapValues { (_, payloadsForId) -> payloadsForId.distinctBy { it.recipeId } }
    }
}
