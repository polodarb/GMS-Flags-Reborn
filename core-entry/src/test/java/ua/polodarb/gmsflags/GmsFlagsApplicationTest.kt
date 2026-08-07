package ua.polodarb.gmsflags

import org.junit.Assert.assertEquals
import org.junit.Test

class GmsFlagsApplicationTest {

    @Test
    fun `blank trusted constant is unknown`() {
        assertEquals("unknown", officialBuildStatus(trusted = "", actual = "AB:CD"))
    }

    @Test
    fun `unreadable own signature is unknown`() {
        assertEquals("unknown", officialBuildStatus(trusted = "AB:CD", actual = null))
    }

    @Test
    fun `matching signature is official`() {
        assertEquals("true", officialBuildStatus(trusted = "AB:CD", actual = "ab:cd"))
    }

    @Test
    fun `mismatching signature is not official`() {
        assertEquals("false", officialBuildStatus(trusted = "AB:CD", actual = "EF:01"))
    }
}
