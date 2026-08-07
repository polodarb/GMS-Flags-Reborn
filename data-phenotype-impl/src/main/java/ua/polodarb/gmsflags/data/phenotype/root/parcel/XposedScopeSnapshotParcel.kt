package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class XposedScopeSnapshotParcel(
    val moduleFound: Boolean,
    val moduleEnabled: Boolean,
    val scopedPackageNames: List<String>,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        moduleFound = parcel.readInt() == 1,
        moduleEnabled = parcel.readInt() == 1,
        scopedPackageNames = parcel.createStringArrayList().orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (moduleFound) 1 else 0)
        parcel.writeInt(if (moduleEnabled) 1 else 0)
        parcel.writeStringList(scopedPackageNames)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<XposedScopeSnapshotParcel> {
        override fun createFromParcel(parcel: Parcel) = XposedScopeSnapshotParcel(parcel)
        override fun newArray(size: Int): Array<XposedScopeSnapshotParcel?> = arrayOfNulls(size)
    }
}
