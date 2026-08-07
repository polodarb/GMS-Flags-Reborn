package ua.polodarb.xposed.info

import org.junit.Assert.assertEquals
import org.junit.Test

class PhenotypePackageCandidatesTest {
    @Test
    fun `composite identity yields itself, its bare namespace, then the context package`() {
        val candidates = phenotypePackageCandidates(
            identityPackageName = "com.google.android.apps.search.assistant.mobile.user#com.google.android.googlequicksearchbox",
            contextPackageName = "com.google.android.googlequicksearchbox",
        )

        assertEquals(
            listOf(
                "com.google.android.apps.search.assistant.mobile.user#com.google.android.googlequicksearchbox",
                "com.google.android.apps.search.assistant.mobile.user",
                "com.google.android.googlequicksearchbox",
            ),
            candidates.map(PhenotypePackageCandidate::packageName),
        )
    }

    @Test
    fun `bare identity yields itself, an identity-context guess, then the context package`() {
        val candidates = phenotypePackageCandidates(
            identityPackageName = "com.google.android.apps.foo",
            contextPackageName = "com.google.android.foo",
        )

        assertEquals(
            listOf(
                "com.google.android.apps.foo",
                "com.google.android.apps.foo#com.google.android.foo",
                "com.google.android.foo",
            ),
            candidates.map(PhenotypePackageCandidate::packageName),
        )
    }

    @Test
    fun `deduplicates when the identity already equals the context package`() {
        val candidates = phenotypePackageCandidates(
            identityPackageName = "com.google.android.foo",
            contextPackageName = "com.google.android.foo",
        )

        assertEquals(
            listOf("com.google.android.foo", "com.google.android.foo#com.google.android.foo"),
            candidates.map(PhenotypePackageCandidate::packageName),
        )
    }
}
