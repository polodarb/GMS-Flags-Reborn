package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import org.junit.Assert.assertEquals
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

class FlagDetailsStateTest {
    private val flags = listOf(
        PhenotypeFlag("alpha_enabled", FlagType.Boolean, "1", "1", false),
        PhenotypeFlag("beta_enabled", FlagType.Boolean, "0", "1", true),
        PhenotypeFlag("disabled", FlagType.Boolean, "0", "0", false),
        PhenotypeFlag("count", FlagType.Integer, "1", "2", true),
    )

    @Test
    fun `visible flags are filtered and sorted from a single source list`() {
        val state = FlagDetailsState(
            androidPackageName = "com.google.test",
            phenotypePackageName = "test",
            loading = false,
            flags = flags,
        )

        assertEquals(
            listOf("alpha_enabled", "beta_enabled"),
            state.copy(filter = FlagFilter.Enabled).visibleFlags().map { it.name },
        )
        assertEquals(
            listOf("beta_enabled"),
            state.copy(filter = FlagFilter.Changed).visibleFlags().map { it.name },
        )
        assertEquals(
            listOf("count"),
            state.copy(selectedType = FlagType.Integer, effectiveQuery = "cou")
                .visibleFlags()
                .map { it.name },
        )
    }
}
