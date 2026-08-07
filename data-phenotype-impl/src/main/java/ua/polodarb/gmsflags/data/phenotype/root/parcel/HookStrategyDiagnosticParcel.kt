package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class HookStrategyDiagnosticParcel(
    val name: String,
    val state: String,
    val appliedCount: Int,
    val consumedCount: Int,
    val message: String?,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        name = parcel.readString().orEmpty(),
        state = parcel.readString().orEmpty(),
        appliedCount = parcel.readInt(),
        consumedCount = parcel.readInt(),
        message = parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(state)
        parcel.writeInt(appliedCount)
        parcel.writeInt(consumedCount)
        parcel.writeString(message)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<HookStrategyDiagnosticParcel> {
        override fun createFromParcel(parcel: Parcel) = HookStrategyDiagnosticParcel(parcel)
        override fun newArray(size: Int): Array<HookStrategyDiagnosticParcel?> = arrayOfNulls(size)
    }
}
