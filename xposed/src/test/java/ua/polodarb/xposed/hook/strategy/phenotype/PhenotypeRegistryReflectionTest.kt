package ua.polodarb.xposed.hook.strategy.phenotype

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PhenotypeRegistryReflectionTest {
    @Test
    fun `finds one explicit lookup and rejects ambiguity`() {
        assertNotNull(
            PhenotypeRegistryReflection.findLookupMethod(ExplicitRegistry::class.java)
        )
        assertNull(
            PhenotypeRegistryReflection.findLookupMethod(AmbiguousRegistry::class.java)
        )
    }

    @Test
    fun `finds bound lookup with one flag argument`() {
        assertNotNull(
            PhenotypeRegistryReflection.findBoundLookupMethod(BoundRegistry::class.java)
        )
        assertNull(
            PhenotypeRegistryReflection.findLookupMethod(BoundRegistry::class.java)
        )
    }

    @Test
    fun `supports legacy and tagged integer wrappers`() {
        assertEquals(
            PhenotypeRegistryReflection.TypeEncoding.Integer,
            PhenotypeRegistryReflection.findValueConstructor(LegacyValue::class.java)
                ?.typeEncoding,
        )
        assertEquals(
            PhenotypeRegistryReflection.TypeEncoding.TaggedInteger,
            PhenotypeRegistryReflection.findValueConstructor(TaggedValue::class.java)
                ?.typeEncoding,
        )
    }

    @Test
    fun `rejects an unsupported wrapper constructor`() {
        assertNull(
            PhenotypeRegistryReflection.findValueConstructor(UnsupportedValue::class.java)
        )
    }

    private fun interface Supplier {
        fun get(): Any?
    }

    private class LegacyValue(
        val supplier: Supplier,
        val type: Int,
        val metadata: Any?,
        val directBootAware: Boolean,
    )

    private class TaggedValue(
        val supplier: Supplier,
        val type: Int,
        val metadata: Metadata?,
    )

    private class UnsupportedValue(
        val supplier: Supplier,
        val type: String,
    )

    private interface Metadata

    @Suppress("UNUSED_PARAMETER")
    private class ExplicitRegistry {
        fun lookup(packageName: String, flagName: String): LegacyValue =
            LegacyValue(Supplier { null }, 1, null, false)
    }

    @Suppress("UNUSED_PARAMETER")
    private class AmbiguousRegistry {
        fun first(packageName: String, flagName: String): LegacyValue =
            LegacyValue(Supplier { null }, 1, null, false)

        fun second(packageName: String, flagName: String): LegacyValue =
            LegacyValue(Supplier { null }, 1, null, false)
    }

    @Suppress("UNUSED_PARAMETER")
    private class BoundRegistry {
        fun lookup(flagName: String): LegacyValue =
            LegacyValue(Supplier { null }, 1, null, false)
    }
}
