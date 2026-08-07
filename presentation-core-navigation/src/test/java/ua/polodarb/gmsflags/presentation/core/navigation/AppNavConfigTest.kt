package ua.polodarb.gmsflags.presentation.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppNavConfigTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `every navigation destination has a saved state serializer`() {
        destinations.forEach { destination ->
            assertNotNull(
                "Missing serializer for ${destination::class.simpleName}",
                AppNavConfig.serializersModule.getPolymorphic(
                    baseClass = NavKey::class,
                    value = destination,
                ),
            )
        }
    }

    private val destinations = listOf<NavKey>(
        RootDestination.BottomBarFlow,
        RootDestination.FlagDetails(
            androidPackageName = "com.example.app",
            applicationName = "Example",
            phenotypePackageName = "example.flags",
            availablePhenotypePackageNames = listOf("example.flags"),
        ),
        RootDestination.AddMultipleFlags(
            androidPackageName = "com.example.app",
            phenotypePackageName = "example.flags",
        ),
        RootDestination.ImportFlags(
            androidPackageName = "com.example.app",
            applicationName = "Example",
            currentPhenotypePackageName = "example.flags",
            supportedPhenotypePackageNames = listOf("example.flags"),
            documentUri = "content://example/flags",
        ),
        RootDestination.ExternalImportFlags(documentUri = "content://example/flags"),
        RootDestination.HookStatus,
        RootDestination.HookStatusDetails(androidPackageName = "com.example.app"),
        BottomBarDestination.Suggestions,
        BottomBarDestination.Apps,
        BottomBarDestination.GmsInsight,
        BottomBarDestination.Experimental,
    )
}
