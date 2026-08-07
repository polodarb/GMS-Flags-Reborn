package ua.polodarb.xposed.info.needle

/** A candidate recipe whose `when` condition actually evaluated to true for one specific call. */
data class MatchedRecipe(val recipeId: String, val value: String)

/** Outcome of evaluating every candidate recipe whose `when` condition references a specific,
 * already-resolved resource id, against the current call's runtime state. [Conflict] means two or
 * more candidates matched with different values - fail closed, apply neither. [Applied]'s
 * [Applied.redundantMatchCount] counts extra matches with the same value, for logging only. */
sealed interface ResourceStringDispatchResult {
    data object NoMatch : ResourceStringDispatchResult
    data class Applied(val value: String, val redundantMatchCount: Int) : ResourceStringDispatchResult
    data class Conflict(val matches: List<MatchedRecipe>) : ResourceStringDispatchResult
}

/**
 * Resolves which (if any) of several STRING_RESULT candidates targeting the same resource id
 * should apply, by evaluating each candidate's *full* `when` condition against [context] - not
 * just the RESOURCE_ID leaf used to narrow [candidates] down, since other conditions (e.g.
 * FLAG_OVERRIDE) can make two candidates on the same resource mutually exclusive rather than
 * conflicting. Fails closed (not-matched) on any evaluation/decode error or non-String value,
 * rather than trusting an earlier validation pass already ran.
 */
object ResourceStringConflictResolver {
    fun resolve(
        candidates: List<NeedleRecipePayload>,
        context: NeedleEvaluationContext,
    ): ResourceStringDispatchResult {
        val matches = candidates.mapNotNull { payload -> matchOrNull(payload, context) }
        return when {
            matches.isEmpty() -> ResourceStringDispatchResult.NoMatch
            matches.map { it.value }.distinct().size == 1 ->
                ResourceStringDispatchResult.Applied(matches.first().value, redundantMatchCount = matches.size - 1)
            else -> ResourceStringDispatchResult.Conflict(matches)
        }
    }

    private fun matchOrNull(payload: NeedleRecipePayload, context: NeedleEvaluationContext): MatchedRecipe? {
        val condition = payload.effect.`when` ?: return null
        val conditionMatches = runCatching { NeedleExpressionEvaluator.evaluateBoolean(condition, context) }.getOrDefault(false)
        if (!conditionMatches) return null
        val valueExpression = runCatching {
            NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), payload.effect.expression)
        }.getOrNull() ?: return null
        val value = NeedleExpressionEvaluator.evaluateValue(valueExpression.value, context) as? String ?: return null
        return MatchedRecipe(payload.recipeId, value)
    }
}
