package ua.polodarb.xposed.info

object HookDiagnosticContract {
    const val COMPATIBILITY_WARNING_PAIRIP_CORE = "pairip_core"

    const val STATE_STARTED = "started"
    const val STATE_INSTALLED = "installed"
    const val STATE_NO_OVERRIDES = "no_overrides"
    const val STATE_PAUSED = "paused"
    const val STATE_FAILED = "failed"

    const val STRATEGY_PHENOTYPE_RUNTIME = "phenotype_runtime"
    const val STRATEGY_PHENOTYPE_REGISTRY = "phenotype_registry"
    const val STRATEGY_MAPS_CLIENT_PARAMETERS = "maps_client_parameters"
    const val STRATEGY_FLUTTER_PHENOTYPE = "flutter_phenotype"
    const val STRATEGY_FINSKY = "finsky"
    const val STRATEGY_INPUT_METHOD_FLAG = "input_method_flag"
    const val STRATEGY_DEVICE_CONFIG_FLAG = "device_config_flag"

    const val STRATEGY_STATE_PENDING = "pending"
    const val STRATEGY_STATE_INSTALLED = "installed"
    const val STRATEGY_STATE_UNAVAILABLE = "unavailable"
    const val STRATEGY_STATE_FAILED = "failed"
}
