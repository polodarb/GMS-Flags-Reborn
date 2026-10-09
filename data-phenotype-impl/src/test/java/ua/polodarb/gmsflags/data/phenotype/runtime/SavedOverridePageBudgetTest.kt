package ua.polodarb.gmsflags.data.phenotype.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.phenotype.root.flags.PhenotypeOverridePager
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel

class SavedOverridePageBudgetTest {
    @Test
    fun `a flag at the write limit fits a backup page with package metadata`() {
        val name = "f"
        val value = "x".repeat(131007)
        val flag = PhenotypeFlagParcel(name, 3, null, value, true)
        assertEquals(1, PhenotypeOverridePager().pages(listOf(flag)).size)
        val budget = SavedOverridePageBudget()
        assertTrue(budget.add("com.example.custom", name, value))
        assertFalse(budget.add("com.example.custom", name, value))
    }
}
