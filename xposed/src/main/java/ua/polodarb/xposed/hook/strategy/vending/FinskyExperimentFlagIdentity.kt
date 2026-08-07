package ua.polodarb.xposed.hook.strategy.vending

internal data class FinskyExperimentFlagIdentity(
    val flagName: String,
) {
    companion object {
        fun fromArguments(arguments: Array<out Any?>): FinskyExperimentFlagIdentity? {
            val featureName = arguments.getOrNull(0) as? String ?: return null
            val parameterName = arguments.getOrNull(1) as? String ?: return null
            if (featureName.isBlank() || parameterName.isBlank()) return null

            return FinskyExperimentFlagIdentity("${featureName}__${parameterName}")
        }
    }
}
