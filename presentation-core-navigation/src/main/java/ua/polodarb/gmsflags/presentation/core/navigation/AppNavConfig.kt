package ua.polodarb.gmsflags.presentation.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

object AppNavConfig {
    val serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(RootDestination.BottomBarFlow::class, RootDestination.BottomBarFlow.serializer())
            subclass(RootDestination.FlagDetails::class, RootDestination.FlagDetails.serializer())
            subclass(RootDestination.AddMultipleFlags::class, RootDestination.AddMultipleFlags.serializer())
            subclass(RootDestination.ImportFlags::class, RootDestination.ImportFlags.serializer())
            subclass(RootDestination.ExternalImportFlags::class, RootDestination.ExternalImportFlags.serializer())
            subclass(RootDestination.HookStatus::class, RootDestination.HookStatus.serializer())
            subclass(RootDestination.Settings::class, RootDestination.Settings.serializer())
            subclass(RootDestination.OverridesStorage::class, RootDestination.OverridesStorage.serializer())
            subclass(RootDestination.HookStatusDetails::class, RootDestination.HookStatusDetails.serializer())
            subclass(RootDestination.RecommendationDetails::class, RootDestination.RecommendationDetails.serializer())
            subclass(RootDestination.CommunityDetails::class, RootDestination.CommunityDetails.serializer())
            subclass(BottomBarDestination.Suggestions::class, BottomBarDestination.Suggestions.serializer())
            subclass(BottomBarDestination.Apps::class, BottomBarDestination.Apps.serializer())
            subclass(BottomBarDestination.Community::class, BottomBarDestination.Community.serializer())
            subclass(BottomBarDestination.GmsInsight::class, BottomBarDestination.GmsInsight.serializer())

            subclass(BottomBarDestination.Experimental::class, BottomBarDestination.Experimental.serializer())
        }
    }

    val config = SavedStateConfiguration {
        serializersModule = AppNavConfig.serializersModule
    }
}
