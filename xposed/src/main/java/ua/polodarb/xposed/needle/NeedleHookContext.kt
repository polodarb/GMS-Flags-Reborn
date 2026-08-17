package ua.polodarb.xposed.needle

import android.content.Context
import android.content.res.Resources
import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import ua.polodarb.xposed.info.needle.ConstantValueType
import ua.polodarb.xposed.info.needle.NeedleBooleans
import ua.polodarb.xposed.info.needle.NeedleEvaluationContext
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore

internal class NeedleHookContext(
    private val param: XC_MethodHook.MethodHookParam,
    private val contextPackageName: String,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val appContext: Context?,
) : NeedleEvaluationContext {
    override fun originalResult(): Any? = param.result
    override fun argument(index: Int): Any? = param.args?.getOrNull(index)

    override fun flagOverride(packageName: String?, flagName: String?, valueType: ConstantValueType?): Any? {
        if (packageName == null || flagName == null) return null
        val match = overrideStore.findBestMatch(
            identityPackageName = packageName,
            contextPackageName = contextPackageName,
            flagName = flagName,
        )
        val override = match.override ?: return null
        return when (valueType) {
            ConstantValueType.BOOL -> NeedleBooleans.parse(override.value)
            ConstantValueType.INT -> override.value.toIntOrNull()
            ConstantValueType.LONG -> override.value.toLongOrNull()
            ConstantValueType.FLOAT -> override.value.toFloatOrNull()
            ConstantValueType.DOUBLE -> override.value.toDoubleOrNull()
            ConstantValueType.STRING, ConstantValueType.BYTES, null -> override.value
        }
    }

    override fun systemFeature(name: String?): Boolean {
        if (name == null) return false
        return appContext?.packageManager?.hasSystemFeature(name) ?: false
    }
    override fun sdkInt(): Int = Build.VERSION.SDK_INT

    override fun resourceId(packageName: String?, resourceType: String?, resourceName: String?): Int? {
        if (resourceType == null || resourceName == null) return null
        val resources = param.thisObject as? Resources ?: return null
        val id = runCatching { resources.getIdentifier(resourceName, resourceType, packageName) }.getOrDefault(0)
        return id.takeIf { it != 0 }
    }
}
