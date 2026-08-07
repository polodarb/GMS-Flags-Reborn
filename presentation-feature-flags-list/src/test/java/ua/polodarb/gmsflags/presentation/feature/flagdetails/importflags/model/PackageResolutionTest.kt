package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType

class PackageResolutionTest {
    private val supportedPackages = setOf(
        "com.google.android.gms.phenotype#com.google.android.apps.foo",
        "com.google.android.apps.bar",
    )

    @Test
    fun `resolvePackageName matches exactly`() {
        assertEquals("com.google.android.apps.bar", resolvePackageName("com.google.android.apps.bar", supportedPackages))
    }

    @Test
    fun `resolvePackageName matches on the hash suffix`() {
        assertEquals(
            "com.google.android.gms.phenotype#com.google.android.apps.foo",
            resolvePackageName("com.google.android.apps.foo", supportedPackages),
        )
    }

    @Test
    fun `resolvePackageName returns null when nothing matches`() {
        assertNull(resolvePackageName("com.google.android.apps.unknown", supportedPackages))
    }

    @Test
    fun `flag without its own package resolves to the primary package`() {
        val flag = ImportedFlag("f", FlagType.Boolean, "1")
        assertEquals(
            "com.google.android.apps.bar",
            flag.resolvedPackageName("com.google.android.apps.bar", supportedPackages),
        )
    }

    @Test
    fun `flag with its own package resolves independently of the primary package`() {
        val flag = ImportedFlag("f", FlagType.Boolean, "1", packageName = "com.google.android.apps.foo")
        assertEquals(
            "com.google.android.gms.phenotype#com.google.android.apps.foo",
            flag.resolvedPackageName("com.google.android.apps.bar", supportedPackages),
        )
    }

    @Test
    fun `flag with a package unknown to this app trusts its own package verbatim`() {
        val flag = ImportedFlag("f", FlagType.Boolean, "1", packageName = "com.google.android.libraries.surveys")
        assertEquals(
            "com.google.android.libraries.surveys",
            flag.resolvedPackageName("com.google.android.apps.bar", supportedPackages),
        )
    }
}
