package ua.polodarb.gmsflags.domain.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType

class XmlFlagsBackupCodecTest {
    private val codec = XmlFlagsBackupCodec()

    @Test
    fun `round trips every type and exact string contents across apps and packages`() {
        val backup = FlagsBackup(
            listOf(
                BackupPackage(
                    "com.test.one",
                    "custom#com.test.one",
                    listOf(
                        FlagOverride("boolean", FlagType.Boolean, "0"),
                        FlagOverride("integer", FlagType.Integer, "18446744073709551615"),
                        FlagOverride("float", FlagType.Float, "-1.25"),
                        FlagOverride("string", FlagType.String, "  &\"<>\t\n\r🦊  "),
                    ),
                ),
                BackupPackage(
                    "com.test.two",
                    "other",
                    listOf(FlagOverride("empty", FlagType.String, "")),
                ),
            )
        )
        val encoded = codec.encode(backup)
        assertTrue(encoded.contains("version=\"1\""))
        assertTrue(codec.isBackup(encoded))
        assertEquals(
            backup.copy(
                packages = backup.packages.map { it.copy(flags = it.flags.sortedBy { flag -> flag.name }) }
            ),
            codec.decode(encoded),
        )
    }

    @Test
    fun `large writable string stays exportable despite package metadata`() {
        val backup = FlagsBackup(
            listOf(
                BackupPackage(
                    "com.test.app",
                    "com.example.custom",
                    listOf(FlagOverride("f", FlagType.String, "x".repeat(131000))),
                )
            )
        )
        assertEquals(backup, codec.decode(codec.encode(backup)))
    }

    @Test
    fun `rejects unsupported or malformed encoded values`() {
        val template =
            "<gms-flags-backup version=\"2\"><package name=\"custom\" androidPackage=\"com.test.app\"><flags><flag name=\"f\" type=\"string\" value=\"AAA=\" valueEncoding=\"base64-utf16be\" /></flags></package></gms-flags-backup>"
        val cases = listOf(
            template.replace("version=\"2\"", "version=\"1\""),
            template.replace("base64-utf16be", "unknown"),
            template.replace("AAA=", "!invalid"),
            template.replace("AAA=", "AA=="),
            template.replace("type=\"string\"", "type=\"integer\""),
        )
        cases.forEach { assertTrue(runCatching { codec.decode(it) }.isFailure) }
    }

    @Test
    fun `legacy document remains distinguishable`() {
        assertFalse(
            codec.isBackup(
                "<package name=\"legacy\"><flags><flag name=\"a\" type=\"boolean\" value=\"true\"/></flags></package>"
            )
        )
    }

    @Test
    fun `rejects invalid and unsafe backups before restore`() {
        val valid = codec.encode(
            FlagsBackup(
                listOf(
                    BackupPackage(
                        "com.test.app",
                        "custom",
                        listOf(FlagOverride("flag", FlagType.Integer, "2")),
                    )
                )
            )
        )
        val cases = listOf(
            valid.replace("version=\"1\"", "version=\"3\""),
            valid.replace("value=\"2\"", "value=\"invalid\""),
            valid.replace("type=\"integer\"", "type=\"unknown\""),
            valid.replace(
                "</flags>",
                "<flag name=\"flag\" type=\"string\" value=\"different\"/></flags>",
            ),
            valid.replace("androidPackage=\"com.test.app\"", "androidPackage=\"bad/path\""),
            "<!DOCTYPE foo [<!ENTITY x SYSTEM 'file:///etc/passwd'>]>" + valid,
            "<gms-flags-backup version=\"1\"><other/></gms-flags-backup>",
            "<gms-flags-backup",
        )
        cases.forEach { xml ->
            assertTrue("Should reject $xml", runCatching { codec.decode(xml) }.isFailure)
        }
    }

    @Test
    fun `empty backup is readable and cannot erase flags`() {
        assertEquals(0, codec.decode(codec.encode(FlagsBackup(emptyList()))).flagCount)
    }

    @Test
    fun `round trips XML unsupported characters and surrogate code units with version two`() {
        val backup = FlagsBackup(
            listOf(
                BackupPackage(
                    "com.test.app",
                    "custom",
                    listOf(FlagOverride("flag", FlagType.String, "\u0000\u000b\ud800x\udfff")),
                )
            )
        )
        val xml = codec.encode(backup)
        assertTrue(xml.contains("version=\"2\""))
        assertTrue(xml.contains("valueEncoding=\"base64-utf16be\""))
        assertEquals(backup, codec.decode(xml))
    }
}
