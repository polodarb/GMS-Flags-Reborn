package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class SavedOverrideParcel(
    val packageName: String,
    val flag: PhenotypeFlagParcel,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        packageName = parcel.readString().orEmpty(),
        flag = PhenotypeFlagParcel.createFromParcel(parcel),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        flag.writeToParcel(parcel, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<SavedOverrideParcel> {
        override fun createFromParcel(parcel: Parcel) = SavedOverrideParcel(parcel)

        override fun newArray(size: Int): Array<SavedOverrideParcel?> = arrayOfNulls(size)
    }
}
