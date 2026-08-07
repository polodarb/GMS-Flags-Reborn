package ua.polodarb.xposed.hook.strategy.phenotype

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BoundPhenotypeRegistryIdentityResolverTest {
    @Test
    fun `resolves the only package in the bound registry`() {
        val receiver = Receiver(
            Registry(mapOf("credentialmanager_android" to Any()), Any())
        )

        assertEquals(
            "credentialmanager_android",
            BoundPhenotypeRegistryIdentityResolver.findPackageName(receiver),
        )
    }

    @Test
    fun `fails closed when several packages are bound`() {
        val receiver = Receiver(
            Registry(
                mapOf(
                    "first_package" to Any(),
                    "second_package" to Any(),
                ),
                Any(),
            )
        )

        assertNull(BoundPhenotypeRegistryIdentityResolver.findPackageName(receiver))
    }

    @Test
    fun `fails closed when the receiver shape is ambiguous`() {
        val receiver = AmbiguousReceiver(
            Registry(mapOf("only_package" to Any()), Any()),
            Any(),
        )

        assertNull(BoundPhenotypeRegistryIdentityResolver.findPackageName(receiver))
    }

    private class Receiver(val registry: Registry)

    private class AmbiguousReceiver(
        val registry: Registry,
        val other: Any,
    )

    private class Registry(
        val packages: Map<String, Any>,
        val other: Any,
    )
}
