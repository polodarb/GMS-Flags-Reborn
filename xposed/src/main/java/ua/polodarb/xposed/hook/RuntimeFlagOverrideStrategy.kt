package ua.polodarb.xposed.hook

import org.luckypray.dexkit.DexKitBridge

internal interface RuntimeFlagOverrideStrategy {
    val diagnosticName: String
    fun install(bridge: DexKitBridge)
}
