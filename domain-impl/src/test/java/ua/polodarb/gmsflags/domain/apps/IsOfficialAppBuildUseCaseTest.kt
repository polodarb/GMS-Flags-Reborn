package ua.polodarb.gmsflags.domain.apps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsOfficialAppBuildUseCaseTest {

    @Test
    fun `debug build is always trusted even with a mismatching signature`() {
        val isOfficialAppBuild = IsOfficialAppBuildUseCase(
            trustedSignatureSha256 = "AB:CD",
            actualSignatureSha256 = "EF:01",
            isDebugBuild = true,
        )

        assertTrue(isOfficialAppBuild())
    }

    @Test
    fun `matching signature is trusted`() {
        val isOfficialAppBuild = IsOfficialAppBuildUseCase(
            trustedSignatureSha256 = "AB:CD",
            actualSignatureSha256 = "ab:cd",
            isDebugBuild = false,
        )

        assertTrue(isOfficialAppBuild())
    }

    @Test
    fun `mismatching signature on a non-debug build is not trusted`() {
        val isOfficialAppBuild = IsOfficialAppBuildUseCase(
            trustedSignatureSha256 = "AB:CD",
            actualSignatureSha256 = "EF:01",
            isDebugBuild = false,
        )

        assertFalse(isOfficialAppBuild())
    }

    @Test
    fun `nothing to compare against counts as trusted, not blocking`() {
        assertTrue(
            IsOfficialAppBuildUseCase(
                trustedSignatureSha256 = null,
                actualSignatureSha256 = "EF:01",
                isDebugBuild = false,
            )()
        )
        assertTrue(
            IsOfficialAppBuildUseCase(
                trustedSignatureSha256 = "AB:CD",
                actualSignatureSha256 = null,
                isDebugBuild = false,
            )()
        )
    }
}
