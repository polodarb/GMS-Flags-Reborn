package ua.polodarb.xposed.info.needle

object NeedleTypeNames {
    const val BOOLEAN_PRIMITIVE = "boolean"
    const val OBJECT = "java.lang.Object"
    const val BOXED_BOOLEAN = "java.lang.Boolean"
    const val STRING = "java.lang.String"
    const val CHAR_SEQUENCE = "java.lang.CharSequence"

    val PRIMITIVES: Set<String> = setOf(
        "boolean", "byte", "short", "char", "int", "long", "float", "double", "void",
    )

    val NUMERIC_PRIMITIVES: Set<String> = setOf("byte", "short", "char", "int", "long", "float", "double")

    val REFERENCES: Set<String> = setOf(
        OBJECT,
        BOXED_BOOLEAN,
        STRING,
        CHAR_SEQUENCE,
        "java.lang.Integer",
        "java.lang.Long",
        "java.util.Locale",
        "android.content.Intent",
        "android.os.Bundle",
    )

    val ALL: Set<String> = PRIMITIVES + REFERENCES

    fun isKnown(name: String): Boolean = name in ALL

    private val WELL_FORMED = Regex("^[A-Za-z_\$][A-Za-z0-9_\$]*(\\.[A-Za-z_\$][A-Za-z0-9_\$]*)*(\\[\\])*$")

    fun isWellFormed(name: String): Boolean = name in PRIMITIVES || WELL_FORMED.matches(name)
}

object NeedleModifierNames {
    val SUPPORTED: Set<String> = setOf("public", "private", "protected", "static", "final")

    fun isKnown(name: String): Boolean = name in SUPPORTED
}

sealed interface NeedleEffectTypeCheck {
    data object Allowed : NeedleEffectTypeCheck
    data class Rejected(val reason: String) : NeedleEffectTypeCheck
}

object NeedleEffectTypeRules {

    fun booleanResult(
        returnTypeName: String,
        semanticResultType: String?,
        hookPoint: HookPoint,
    ): NeedleEffectTypeCheck = when (returnTypeName) {
        NeedleTypeNames.BOOLEAN_PRIMITIVE, NeedleTypeNames.BOXED_BOOLEAN -> {
            if (semanticResultType != null && semanticResultType != NeedleTypeNames.BOXED_BOOLEAN) {
                NeedleEffectTypeCheck.Rejected(
                    "semantic_result_type '$semanticResultType' does not apply to a method returning $returnTypeName",
                )
            } else {
                NeedleEffectTypeCheck.Allowed
            }
        }

        NeedleTypeNames.OBJECT -> when {
            semanticResultType != NeedleTypeNames.BOXED_BOOLEAN -> NeedleEffectTypeCheck.Rejected(
                "BOOLEAN_RESULT on a method returning ${NeedleTypeNames.OBJECT} requires " +
                    "semantic_result_type '${NeedleTypeNames.BOXED_BOOLEAN}'; writing a Boolean into an " +
                    "unconstrained Object bridge would surface as a ClassCastException in the target's caller, " +
                    "which no hook-side catch can intercept",
            )

            hookPoint != HookPoint.AFTER -> NeedleEffectTypeCheck.Rejected(
                "BOOLEAN_RESULT on an ${NeedleTypeNames.OBJECT} bridge requires hook_point AFTER, so the " +
                    "original result is available to type-check before it is replaced; got $hookPoint",
            )

            else -> NeedleEffectTypeCheck.Allowed
        }

        else -> NeedleEffectTypeCheck.Rejected(
            "BOOLEAN_RESULT requires a method returning boolean, ${NeedleTypeNames.BOXED_BOOLEAN} or a " +
                "semantically-Boolean ${NeedleTypeNames.OBJECT} bridge; got '$returnTypeName'",
        )
    }

    fun numericResult(returnTypeName: String): NeedleEffectTypeCheck =
        if (returnTypeName in NeedleTypeNames.NUMERIC_PRIMITIVES) {
            NeedleEffectTypeCheck.Allowed
        } else {
            NeedleEffectTypeCheck.Rejected(
                "NUMERIC_RESULT requires a numeric primitive return type, got '$returnTypeName'",
            )
        }

    fun argumentReplace(parameterTypeName: String, valueType: ConstantValueType?): NeedleEffectTypeCheck {
        if (valueType == null) {
            return NeedleEffectTypeCheck.Rejected("ARGUMENT_REPLACE requires an explicit value_type")
        }
        val accepted = ACCEPTED_CONSTANT_TYPES[parameterTypeName]
            ?: return NeedleEffectTypeCheck.Rejected(
                "ARGUMENT_REPLACE cannot write into a parameter of type '$parameterTypeName'",
            )
        return if (valueType in accepted) {
            NeedleEffectTypeCheck.Allowed
        } else {
            NeedleEffectTypeCheck.Rejected(
                "ARGUMENT_REPLACE value_type $valueType is not assignable to a '$parameterTypeName' parameter",
            )
        }
    }

    fun argumentNull(parameterTypeName: String): NeedleEffectTypeCheck =
        if (parameterTypeName in NeedleTypeNames.PRIMITIVES) {
            NeedleEffectTypeCheck.Rejected(
                "ARGUMENT_NULL cannot null a primitive '$parameterTypeName' parameter; only reference types are nullable",
            )
        } else {
            NeedleEffectTypeCheck.Allowed
        }

    fun requiresBoxedBooleanGuard(returnTypeName: String): Boolean = returnTypeName == NeedleTypeNames.OBJECT

    private val ACCEPTED_CONSTANT_TYPES: Map<String, Set<ConstantValueType>> = mapOf(
        "boolean" to setOf(ConstantValueType.BOOL),
        "byte" to setOf(ConstantValueType.INT),
        "short" to setOf(ConstantValueType.INT),
        "char" to setOf(ConstantValueType.INT),
        "int" to setOf(ConstantValueType.INT),
        "long" to setOf(ConstantValueType.LONG),
        "float" to setOf(ConstantValueType.FLOAT),
        "double" to setOf(ConstantValueType.DOUBLE),
        NeedleTypeNames.BOXED_BOOLEAN to setOf(ConstantValueType.BOOL),
        "java.lang.Integer" to setOf(ConstantValueType.INT),
        "java.lang.Long" to setOf(ConstantValueType.LONG),
        NeedleTypeNames.STRING to setOf(ConstantValueType.STRING),
        NeedleTypeNames.CHAR_SEQUENCE to setOf(ConstantValueType.STRING),
    )
}
