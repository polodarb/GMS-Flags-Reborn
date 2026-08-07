package ua.polodarb.gmsflags.domain.flags

enum class FlagType(val storageId: Int) {
    Boolean(0),
    Integer(1),
    Float(2),
    String(3);

    companion object {
        fun fromStorageId(value: Int): FlagType? = entries.firstOrNull { it.storageId == value }
    }
}
