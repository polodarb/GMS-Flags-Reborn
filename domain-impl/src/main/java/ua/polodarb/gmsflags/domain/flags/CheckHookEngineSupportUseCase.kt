package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.domain.server.content.HookEngineSupport
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.xposed.info.needle.NeedleClientSupport

class CheckHookEngineSupportUseCase : HookEngineSupport {
    override fun invoke(hook: RecommendationVariantHook): Boolean {
        val payload = hook.envelope?.payloadBase64 ?: return true
        return NeedleClientSupport.isSupported(payload)
    }
}
