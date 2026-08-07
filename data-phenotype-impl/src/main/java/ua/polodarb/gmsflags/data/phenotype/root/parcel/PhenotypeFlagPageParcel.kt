package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class PhenotypeFlagPageParcel(
    val offset: Int,
    val totalCount: Int,
    val nextOffset: Int,
    val flags: List<PhenotypeFlagParcel>,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        offset = parcel.readInt(),
        totalCount = parcel.readInt(),
        nextOffset = parcel.readInt(),
        flags = parcel.createTypedArrayList(PhenotypeFlagParcel.CREATOR).orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, parcelableFlags: Int) {
        parcel.writeInt(offset)
        parcel.writeInt(totalCount)
        parcel.writeInt(nextOffset)
        parcel.writeTypedList(flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PhenotypeFlagPageParcel> {
        const val END_OF_LIST = -1

        override fun createFromParcel(parcel: Parcel) = PhenotypeFlagPageParcel(parcel)

        override fun newArray(size: Int): Array<PhenotypeFlagPageParcel?> = arrayOfNulls(size)
    }
}
