package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class HookDiagnosticSnapshotParcel(
    val androidPackageName: String,
    val currentOverrideCount: Int,
    val hasSession: Boolean,
    val processName: String,
    val versionCode: Long,
    val startedAt: Long,
    val updatedAt: Long,
    val loadedOverrideCount: Int,
    val state: String,
    val error: String?,
    val strategies: List<HookStrategyDiagnosticParcel>,
    val compatibilityWarnings: List<String>,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        androidPackageName = parcel.readString().orEmpty(),
        currentOverrideCount = parcel.readInt(),
        hasSession = parcel.readInt() == 1,
        processName = parcel.readString().orEmpty(),
        versionCode = parcel.readLong(),
        startedAt = parcel.readLong(),
        updatedAt = parcel.readLong(),
        loadedOverrideCount = parcel.readInt(),
        state = parcel.readString().orEmpty(),
        error = parcel.readString(),
        strategies = parcel.createTypedArrayList(HookStrategyDiagnosticParcel.CREATOR).orEmpty(),
        compatibilityWarnings = parcel.createStringArrayList().orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(androidPackageName)
        parcel.writeInt(currentOverrideCount)
        parcel.writeInt(if (hasSession) 1 else 0)
        parcel.writeString(processName)
        parcel.writeLong(versionCode)
        parcel.writeLong(startedAt)
        parcel.writeLong(updatedAt)
        parcel.writeInt(loadedOverrideCount)
        parcel.writeString(state)
        parcel.writeString(error)
        parcel.writeTypedList(strategies)
        parcel.writeStringList(compatibilityWarnings)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<HookDiagnosticSnapshotParcel> {
        override fun createFromParcel(parcel: Parcel) = HookDiagnosticSnapshotParcel(parcel)
        override fun newArray(size: Int): Array<HookDiagnosticSnapshotParcel?> = arrayOfNulls(size)
    }
}
