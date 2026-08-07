package ua.polodarb.gmsflags.domain.apps

/**
 * Whether this build's own APK signing certificate is safe to trust for applying micro-hooks.
 * Debug builds answer true here even though their signing certificate genuinely differs from the
 * release one - that difference is expected during development, not a compromise signal. A release
 * build whose certificate does NOT match the trusted one answers false: it may be a repackaged or
 * re-signed copy, and a hook's "Verified" recipe-signature badge says nothing about whether the app
 * evaluating it is itself genuine.
 */
fun interface IsOfficialAppBuild {
    operator fun invoke(): Boolean
}
