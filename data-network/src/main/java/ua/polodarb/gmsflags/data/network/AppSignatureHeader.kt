package ua.polodarb.gmsflags.data.network

const val APP_SIGNATURE_SHA256_QUALIFIER_NAME = "appSignatureSha256"

/**
 * Lets the backend flag requests from a repackaged/re-signed client build, since a modded APK
 * necessarily carries a different signing certificate unless the attacker also holds the real
 * release-signing key.
 */
const val APP_SIGNATURE_SHA256_HEADER = "X-App-Signature-SHA256"
