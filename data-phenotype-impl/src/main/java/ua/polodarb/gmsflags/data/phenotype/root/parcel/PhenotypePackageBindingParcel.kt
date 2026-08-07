package ua.polodarb.gmsflags.data.phenotype.root.parcel

import android.os.Parcel
import android.os.Parcelable

internal data class PhenotypePackageBindingParcel(
    val phenotypePackageName: String,
    val androidPackageName: String,
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        phenotypePackageName = parcel.readString().orEmpty(),
        androidPackageName = parcel.readString().orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phenotypePackageName)
        parcel.writeString(androidPackageName)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PhenotypePackageBindingParcel> {
        override fun createFromParcel(parcel: Parcel) = PhenotypePackageBindingParcel(parcel)

        override fun newArray(size: Int): Array<PhenotypePackageBindingParcel?> = arrayOfNulls(size)
    }
}
