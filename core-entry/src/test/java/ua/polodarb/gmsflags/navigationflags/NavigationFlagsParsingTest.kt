package ua.polodarb.gmsflags.navigationflags

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationFlagsParsingTest {
    @Test
    fun `true cache hides the tab`() {
        assertTrue(parseGmsInsightHidden("true"))
    }

    @Test
    fun `false cache shows the tab`() {
        assertFalse(parseGmsInsightHidden("false"))
    }

    @Test
    fun `absent cache shows the tab`() {
        assertFalse(parseGmsInsightHidden(null))
    }

    @Test
    fun `corrupt cache shows the tab`() {
        assertFalse(parseGmsInsightHidden("not_a_boolean"))
        assertFalse(parseGmsInsightHidden(""))
    }
}
