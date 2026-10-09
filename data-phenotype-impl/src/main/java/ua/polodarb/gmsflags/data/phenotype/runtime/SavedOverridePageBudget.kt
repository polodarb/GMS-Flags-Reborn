package ua.polodarb.gmsflags.data.phenotype.runtime

internal class SavedOverridePageBudget {
    private var bytes = 0L

    fun add(packageName: String, name: String, value: String): Boolean {
        val size = 160L + 2L * (packageName.length.toLong() + name.length + value.length)
        require(size <= MAX_BYTES) { "A saved flag is too large for Binder" }
        if (bytes + size > MAX_BYTES) {
            return false
        }
        bytes += size
        return true
    }

    private companion object {
        const val MAX_BYTES = 512 * 1024
    }
}
