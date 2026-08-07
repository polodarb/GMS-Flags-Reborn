package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class MicroHookEnvelopeParcel(
    val recipeId: Long,
    val payloadBase64: String,
    val payloadSha256: String,
    val signatureBase64: String,
    val required: Boolean,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        recipeId = parcel.readLong(),
        payloadBase64 = parcel.readString().orEmpty(),
        payloadSha256 = parcel.readString().orEmpty(),
        signatureBase64 = parcel.readString().orEmpty(),
        required = parcel.readInt() != 0,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(recipeId)
        parcel.writeString(payloadBase64)
        parcel.writeString(payloadSha256)
        parcel.writeString(signatureBase64)
        parcel.writeInt(if (required) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MicroHookEnvelopeParcel> {
        override fun createFromParcel(parcel: Parcel) = MicroHookEnvelopeParcel(parcel)

        override fun newArray(size: Int): Array<MicroHookEnvelopeParcel?> = arrayOfNulls(size)
    }
}
