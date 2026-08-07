package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class PhenotypeFlagParcel(
    val name: String,
    val type: Int,
    val originalValue: String?,
    val value: String,
    val overridden: Boolean,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        name = parcel.readString().orEmpty(),
        type = parcel.readInt(),
        originalValue = parcel.readString(),
        value = parcel.readString().orEmpty(),
        overridden = parcel.readInt() != 0,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(type)
        parcel.writeString(originalValue)
        parcel.writeString(value)
        parcel.writeInt(if (overridden) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PhenotypeFlagParcel> {
        override fun createFromParcel(parcel: Parcel) = PhenotypeFlagParcel(parcel)

        override fun newArray(size: Int): Array<PhenotypeFlagParcel?> = arrayOfNulls(size)
    }
}
