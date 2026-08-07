package ua.polodarb.xposed.info.needle

/** Pure evaluator for the typed OR-of-AND-of-comparisons boolean DSL and plain value sources.
 * No Android/Xposed dependency - testable with a fake [NeedleEvaluationContext]. */
object NeedleExpressionEvaluator {

    fun evaluateBoolean(expression: BooleanExpression, context: NeedleEvaluationContext): Boolean =
        expression.orGroups.any { group -> group.andConditions.all { evaluateCondition(it, context) } }

    private fun evaluateCondition(condition: Condition, context: NeedleEvaluationContext): Boolean {
        val left = evaluateValue(condition.left, context)
        val right = evaluateValue(condition.right, context)
        return when (condition.compare) {
            CompareOp.EQ -> left == right
            CompareOp.NEQ -> left != right
            CompareOp.LT -> compareNumeric(left, right) { a, b -> a < b }
            CompareOp.LTE -> compareNumeric(left, right) { a, b -> a <= b }
            CompareOp.GT -> compareNumeric(left, right) { a, b -> a > b }
            CompareOp.GTE -> compareNumeric(left, right) { a, b -> a >= b }
        }
    }

    private inline fun compareNumeric(left: Any?, right: Any?, compare: (Double, Double) -> Boolean): Boolean {
        val leftNumber = (left as? Number)?.toDouble() ?: return false
        val rightNumber = (right as? Number)?.toDouble() ?: return false
        return compare(leftNumber, rightNumber)
    }

    fun evaluateValue(source: TypedValueSource, context: NeedleEvaluationContext): Any? {
        return when (source.source) {
            SourceKind.ORIGINAL_RESULT -> context.originalResult()
            SourceKind.ARGUMENT -> context.argument(source.index ?: return null)
            SourceKind.CONSTANT -> parseConstant(source.valueType, source.value)
            SourceKind.FLAG_OVERRIDE -> context.flagOverride(source.packageName, source.flagName, source.valueType)
            SourceKind.SYSTEM_FEATURE -> context.systemFeature(source.name)
            SourceKind.SDK_INT -> context.sdkInt()
            SourceKind.RESOURCE_ID -> context.resourceId(source.packageName, source.resourceType, source.resourceName)
        }
    }

    private fun parseConstant(valueType: ConstantValueType?, value: String?): Any? {
        if (value == null) return null
        return when (valueType) {
            ConstantValueType.BOOL -> value.toBoolean()
            ConstantValueType.INT -> value.toIntOrNull()
            ConstantValueType.LONG -> value.toLongOrNull()
            ConstantValueType.FLOAT -> value.toFloatOrNull()
            ConstantValueType.DOUBLE -> value.toDoubleOrNull()
            ConstantValueType.STRING, ConstantValueType.BYTES, null -> value
        }
    }
}
