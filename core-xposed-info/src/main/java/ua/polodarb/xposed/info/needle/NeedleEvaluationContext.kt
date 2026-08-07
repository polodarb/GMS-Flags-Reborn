package ua.polodarb.xposed.info.needle

/**
 * Everything a [TypedValueSource] might need to resolve to a concrete value at hook time.
 * Kept as a small interface (not a direct Xposed/Android dependency) so the evaluator in this
 * module stays pure and unit-testable; the `:xposed` module supplies the real implementation.
 */
interface NeedleEvaluationContext {
    fun originalResult(): Any?
    fun argument(index: Int): Any?
    fun flagOverride(packageName: String?, flagName: String?, valueType: ConstantValueType?): Any?
    fun systemFeature(name: String?): Boolean
    fun sdkInt(): Int

    /** Resolves an Android resource id by name (`Resources#getIdentifier`-style), scoped to
     * whatever `Resources` instance is live at hook time. Returns null if [resourceType] or
     * [resourceName] is null, or if the resource cannot be found (implementations must treat
     * `getIdentifier`'s "not found" sentinel, 0, as null - 0 is never a valid resource id). */
    fun resourceId(packageName: String?, resourceType: String?, resourceName: String?): Int?
}
