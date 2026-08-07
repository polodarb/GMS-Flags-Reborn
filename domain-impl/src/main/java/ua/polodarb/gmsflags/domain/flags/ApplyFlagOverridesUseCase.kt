package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository

class ApplyFlagOverridesUseCase(
    private val repository: FlagDetailsRepository,
) : ApplyFlagOverrides {
    override suspend fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<FlagOverride>,
    ): Result<Unit> {
        return runCatching {
            val byIdentity = linkedMapOf<Pair<FlagType, String>, FlagOverride>()
            overrides.forEach { override ->
                val normalized = override.normalized()
                byIdentity[normalized.type to normalized.name] = normalized
            }
            Triple(
                androidPackageName.requireAndroidPackageName(),
                phenotypePackageName.requirePackageName(),
                byIdentity.values.toList(),
            )
        }.fold(
            onSuccess = { (androidPackage, phenotypePackage, normalized) ->
                if (normalized.isEmpty()) Result.success(Unit)
                else repository.applyOverrides(androidPackage, phenotypePackage, normalized)
            },
            onFailure = { Result.failure(it) },
        )
    }
}

private fun FlagOverride.normalized(): FlagOverride {
    val normalizedName = name.trim().also {
        require(it.isNotEmpty()) { "Flag name cannot be empty" }
    }
    val normalizedValue = when (type) {
        FlagType.Boolean -> when {
            value == "1" || value.equals("true", ignoreCase = true) -> "1"
            value == "0" || value.equals("false", ignoreCase = true) -> "0"
            else -> error("Invalid boolean value for $normalizedName")
        }
        FlagType.Integer -> value.trim().also {
            require(it.toLongOrNull() != null || it.toULongOrNull() != null) {
                "Invalid integer value for $normalizedName"
            }
        }
        FlagType.Float -> value.trim().also {
            require(it.toDoubleOrNull() != null) { "Invalid float value for $normalizedName" }
        }
        FlagType.String -> value
    }
    return copy(name = normalizedName, value = normalizedValue)
}
