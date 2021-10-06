package com.baosystems.icrc.psm.data.models

import android.os.Parcel
import android.os.Parcelable
import com.baosystems.icrc.psm.data.TransactionType

class UserIntent(
    val transactionType: TransactionType,
    val facility: IdentifiableModel,
    val transactionDate: String,
    val distributedTo: IdentifiableModel?): Parcelable {

    constructor(parcel: Parcel) : this(
        enumValueOf(parcel.readString()!!),
        // TODO: Find a way to get the OrganisationUnit given the UID,
        //  and include it in the constructor call
        parcel.readParcelable<IdentifiableModel>(
            IdentifiableModel::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readParcelable<IdentifiableModel>(
            IdentifiableModel::class.java.classLoader)
    )

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(transactionType.name)
        // TODO: Uncomment facility OU to write OU info to parcel
        out.writeParcelable(facility, flags)
        out.writeString(transactionDate)
        out.writeParcelable(distributedTo, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<UserIntent> {
        override fun createFromParcel(parcel: Parcel): UserIntent =
            UserIntent(parcel)

        override fun newArray(size: Int): Array<UserIntent?> =
            arrayOfNulls(size)
    }
}