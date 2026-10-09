package ua.polodarb.gmsflags.domain.flags

object FlagOverrideLimits {
    const val MAX_PARCEL_BYTES = 256 * 1024

    fun estimatedParcelBytes(name: String, value: String, originalValue: String? = null): Long =
        128L + 2L * (name.length.toLong() + value.length + (originalValue?.length ?: 0))
}
