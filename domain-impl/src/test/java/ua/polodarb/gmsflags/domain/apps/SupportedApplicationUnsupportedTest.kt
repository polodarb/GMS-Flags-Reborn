package ua.polodarb.gmsflags.domain.apps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedApplicationUnsupportedTest {
    @Test
    fun `null disabled version is always supported`() {
        assertFalse(app(versionCode = 100, disabledFromVersion = null).isUnsupported)
    }

    @Test
    fun `version below the disabled threshold is supported`() {
        assertFalse(app(versionCode = 99, disabledFromVersion = 100).isUnsupported)
    }

    @Test
    fun `version exactly at the disabled threshold is unsupported`() {
        assertTrue(app(versionCode = 100, disabledFromVersion = 100).isUnsupported)
    }

    @Test
    fun `version above the disabled threshold is unsupported`() {
        assertTrue(app(versionCode = 150, disabledFromVersion = 100).isUnsupported)
    }

    private fun app(versionCode: Long, disabledFromVersion: Long?) = SupportedApplication(
        androidPackageName = "com.example.app",
        flagPackages = listOf(FlagPackage("com.example.app", FlagPackageCategory.Primary)),
        name = "Example",
        versionName = null,
        versionCode = versionCode,
        lastUpdateTime = 0,
        disabledFromVersion = disabledFromVersion,
    )
}
