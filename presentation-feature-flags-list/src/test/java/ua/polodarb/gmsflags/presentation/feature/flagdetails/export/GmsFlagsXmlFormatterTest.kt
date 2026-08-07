package ua.polodarb.gmsflags.presentation.feature.flagdetails.export

import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

class GmsFlagsXmlFormatterTest {
    @Test
    fun `keeps legacy boolean format and escapes attributes`() {
        val xml = GmsFlagsXmlFormatter.format(
            packageName = "test&package",
            flags = listOf(
                PhenotypeFlag("a<flag", FlagType.Boolean, "0", "1", true),
                PhenotypeFlag("text", FlagType.String, "", "a\"b", true),
            ),
        )

        assertTrue(xml.contains("name=\"test&amp;package\""))
        assertTrue(xml.contains("name=\"a&lt;flag\" type=\"boolean\" value=\"true\""))
        assertTrue(xml.contains("value=\"a&quot;b\""))
    }
}
