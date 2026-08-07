package ua.polodarb.gmsflags.domain.hookstatus

/**
 * Returns the subset of [androidPackageNames] that bundle PairIP protection, which makes hooking
 * them likely to crash the process. Backed by the same root diagnostics as the hook-status screen.
 */
fun interface GetPairipIncompatiblePackages {
    suspend operator fun invoke(androidPackageNames: List<String>): Result<Set<String>>
}
