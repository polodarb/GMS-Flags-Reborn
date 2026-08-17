package ua.polodarb.gmsflags.domain.flags.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverridesUseCase
import ua.polodarb.gmsflags.domain.flags.ApplyMicroHooks
import ua.polodarb.gmsflags.domain.flags.ApplyMicroHooksUseCase
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverride
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverrideUseCase
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverrides
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverridesUseCase
import ua.polodarb.gmsflags.domain.flags.DeleteMicroHooks
import ua.polodarb.gmsflags.domain.flags.DeleteMicroHooksUseCase
import ua.polodarb.gmsflags.domain.flags.DecodeHookRecipeUseCase
import ua.polodarb.gmsflags.domain.flags.DeletePackageOverrides
import ua.polodarb.gmsflags.domain.flags.DeletePackageOverridesUseCase
import ua.polodarb.gmsflags.domain.flags.GetPhenotypeFlags
import ua.polodarb.gmsflags.domain.flags.GetPhenotypeFlagsUseCase
import ua.polodarb.gmsflags.domain.flags.ObserveFlagOverrideChanges
import ua.polodarb.gmsflags.domain.flags.ObserveFlagOverrideChangesUseCase
import ua.polodarb.gmsflags.domain.flags.VerifyHookTrustUseCase
import ua.polodarb.gmsflags.domain.flags.CheckHookEngineSupportUseCase
import ua.polodarb.gmsflags.domain.server.content.DecodeHookRecipe
import ua.polodarb.gmsflags.domain.server.content.HookEngineSupport
import ua.polodarb.gmsflags.domain.server.content.VerifyHookTrust

/** Qualifier for the injected trusted Needle public key - see :core-entry's di/AppKoin.kt. */
val NEEDLE_TRUSTED_PUBLIC_KEY_QUALIFIER = named("needleTrustedPublicKey")

val flagsDomainModule = module {
    factory<GetPhenotypeFlags> { GetPhenotypeFlagsUseCase(get()) }
    factory<ApplyFlagOverrides> { ApplyFlagOverridesUseCase(get()) }
    factory<ApplyMicroHooks> { ApplyMicroHooksUseCase(get()) }
    factory<DeleteMicroHooks> { DeleteMicroHooksUseCase(get()) }
    factory<DeleteFlagOverride> { DeleteFlagOverrideUseCase(get()) }
    factory<DeleteFlagOverrides> { DeleteFlagOverridesUseCase(get()) }
    factory<DeletePackageOverrides> { DeletePackageOverridesUseCase(get()) }
    factory<ObserveFlagOverrideChanges> { ObserveFlagOverrideChangesUseCase(get()) }
    factory<VerifyHookTrust> {
        VerifyHookTrustUseCase(get(qualifier = NEEDLE_TRUSTED_PUBLIC_KEY_QUALIFIER))
    }
    factory<DecodeHookRecipe> { DecodeHookRecipeUseCase() }
    factory<HookEngineSupport> { CheckHookEngineSupportUseCase() }
}
