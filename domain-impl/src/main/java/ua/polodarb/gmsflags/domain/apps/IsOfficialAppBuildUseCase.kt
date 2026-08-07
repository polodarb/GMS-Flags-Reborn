package ua.polodarb.gmsflags.domain.apps

/**
 * [trustedSignatureSha256] and [actualSignatureSha256] are injected in (never read from
 * BuildConfig directly - domain/domain-impl must not depend on the app module). A missing/blank
 * value on either side means there's nothing to compare, which counts as trusted rather than
 * blocking hooks on a read failure that has nothing to do with the app's actual authenticity.
 */
class IsOfficialAppBuildUseCase(
    private val trustedSignatureSha256: String?,
    private val actualSignatureSha256: String?,
    private val isDebugBuild: Boolean,
) : IsOfficialAppBuild {
    override fun invoke(): Boolean {
        if (isDebugBuild) return true
        val trusted = trustedSignatureSha256?.takeIf(String::isNotBlank) ?: return true
        val actual = actualSignatureSha256?.takeIf(String::isNotBlank) ?: return true
        return actual.equals(trusted, ignoreCase = true)
    }
}
