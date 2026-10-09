package ua.polodarb.gmsflags.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import ua.polodarb.gmsflags.presentation.core.navigation.AppNavConfig
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination

class BackupNavigationSerializationTest {
    private val json = Json { serializersModule = AppNavConfig.serializersModule }
    private val serializer = ListSerializer(PolymorphicSerializer(NavKey::class))

    @Test
    fun `backup settings destination survives saving and restoring the back stack`() {
        assertRoundTrip(RootDestination.FlagsBackup())
    }

    @Test
    fun `backup import destination preserves its document URI`() {
        assertRoundTrip(RootDestination.FlagsBackup("content://test/backup.xml"))
    }

    @Test
    fun `export sub screen survives saving and restoring the back stack`() {
        assertRoundTrip(RootDestination.ExportFlagsBackup)
    }

    @Test
    fun `import sub screen preserves its document URI`() {
        assertRoundTrip(RootDestination.ImportFlagsBackup("content://test/backup.xml"))
        assertRoundTrip(RootDestination.ImportFlagsBackup())
    }

    private fun assertRoundTrip(destination: RootDestination) {
        val backStack: List<NavKey> = listOf(RootDestination.BottomBarFlow, RootDestination.OverridesStorage, destination)
        val encoded = json.encodeToString(serializer, backStack)

        assertEquals(backStack, json.decodeFromString(serializer, encoded))
    }
}
