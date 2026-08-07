package ua.polodarb.xposed.info.needle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** Shared JSON codec for every Needle wire type - lenient to unknown fields so older engines
 * degrade gracefully instead of crashing when a newer backend adds a field. */
val NeedleJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

object NeedleProtocol {
    const val RECIPE_MEDIA_TYPE = "application/vnd.gmsflags.needle-recipe+json;v=1"
    const val SIGNATURE_ALGORITHM = "ECDSA_P256_SHA256"
    const val DOMAIN_SEPARATOR = "GMSFLAGS_NEEDLE_RECIPE_V1 "
}

/** As delivered by GET /gmsflags/v1/apps/{pkg}/resolve's `hooks[]`. */
@Serializable
data class NeedleEnvelope(
    @SerialName("media_type") val mediaType: String,
    @SerialName("payload_base64") val payloadBase64: String,
    @SerialName("payload_sha256") val payloadSha256: String,
    @SerialName("signature_algorithm") val signatureAlgorithm: String,
    @SerialName("signature_base64") val signatureBase64: String,
    val required: Boolean,
)

/** The decoded, signed content of [NeedleEnvelope.payloadBase64] - what the offline signing
 * script actually signs. Its internal shape is a convention between that script and this parser,
 * not validated by the backend (which treats the payload as an opaque signed blob). */
@Serializable
data class NeedleRecipePayload(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("recipe_id") val recipeId: String,
    val codename: String,
    @SerialName("app_package_name") val appPackageName: String,
    @SerialName("process_name") val processName: String? = null,
    val selector: MicroHookSelector,
    val effect: MicroHookEffect,
)

/** [DEX_METHOD] searches the target APK's own dex via DexKit. [ANDROID_RESOURCE_STRING] installs
 * exactly one engine-hardcoded framework hook (`Resources#getString(int)`) - a recipe can only
 * pick which resource id to react to, never an arbitrary framework class/method. */
enum class SelectorKind { DEX_METHOD, ANDROID_RESOURCE_STRING }

@Serializable
data class MicroHookSelector(
    val type: SelectorKind = SelectorKind.DEX_METHOD,
    @SerialName("class_using_strings_all") val classUsingStringsAll: List<String> = emptyList(),
    @SerialName("class_using_strings_any") val classUsingStringsAny: List<String> = emptyList(),
    @SerialName("method_return_type") val methodReturnType: String = "",
    @SerialName("method_parameter_types") val methodParameterTypes: List<String> = emptyList(),
    @SerialName("method_modifiers_all") val methodModifiersAll: List<String> = emptyList(),
    @SerialName("method_using_strings_all") val methodUsingStringsAll: List<String> = emptyList(),
    @SerialName("method_using_strings_any") val methodUsingStringsAny: List<String> = emptyList(),
)

enum class HookPoint { BEFORE, AFTER }

/** [STRING_RESULT] is only ever valid paired with [SelectorKind.ANDROID_RESOURCE_STRING] (enforced
 * by NeedleRecipeValidation) - its resolved method is guaranteed to return String, unlike a
 * DEX_METHOD selector's return type, which is never statically guaranteed. */
enum class EffectKind { BOOLEAN_RESULT, NUMERIC_RESULT, ARGUMENT_REPLACE, STRING_RESULT }
enum class CompareOp { EQ, NEQ, LT, LTE, GT, GTE }
enum class SourceKind { ORIGINAL_RESULT, ARGUMENT, CONSTANT, FLAG_OVERRIDE, SYSTEM_FEATURE, SDK_INT, RESOURCE_ID }
enum class ConstantValueType { BOOL, INT, LONG, FLOAT, DOUBLE, STRING, BYTES }

@Serializable
data class TypedValueSource(
    val source: SourceKind,
    val index: Int? = null,
    @SerialName("value_type") val valueType: ConstantValueType? = null,
    val value: String? = null,
    @SerialName("package_name") val packageName: String? = null,
    @SerialName("flag_name") val flagName: String? = null,
    val name: String? = null,
    /** Only meaningful for [SourceKind.RESOURCE_ID] - resolved via `Resources#getIdentifier`
     * together with [resourceName] and [packageName]. */
    @SerialName("resource_type") val resourceType: String? = null,
    @SerialName("resource_name") val resourceName: String? = null,
)

@Serializable
data class Condition(
    val left: TypedValueSource,
    val compare: CompareOp,
    val right: TypedValueSource,
)

@Serializable
data class AndGroup(@SerialName("and_conditions") val andConditions: List<Condition>)

@Serializable
data class BooleanExpression(@SerialName("or_groups") val orGroups: List<AndGroup>)

@Serializable
data class ValueExpression(val value: TypedValueSource)

@Serializable
data class MicroHookEffect(
    @SerialName("hook_point") val hookPoint: HookPoint,
    val kind: EffectKind,
    @SerialName("argument_index") val argumentIndex: Int? = null,
    /** Raw JSON; shape depends on [kind] ([BooleanExpression] for BOOLEAN_RESULT, [ValueExpression]
     * otherwise) - decoded on demand by the caller once [kind] is known. */
    val expression: JsonElement,
    /** Optional extra gate, evaluated like a BOOLEAN_RESULT expression. Null means unconditional;
     * when present and false, the effect must not run at all. */
    val `when`: BooleanExpression? = null,
)
