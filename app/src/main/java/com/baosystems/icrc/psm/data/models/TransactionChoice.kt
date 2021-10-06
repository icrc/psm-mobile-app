package com.baosystems.icrc.psm.data.models

import android.os.Parcel
import android.os.Parcelable
import com.baosystems.icrc.psm.data.TransactionType

class TransactionChoice(
    private val transactionType: TransactionType,
    // TODO: Uncomment facility OU
//    private val facility: OrganisationUnit,
    private val transactionDate: String,
    private var distributedTo: String?): Parcelable {

    constructor(parcel: Parcel) : this(
        enumValueOf(parcel.readString()!!),
        // TODO: Find a way to get the OrganisationUnit given the UID,
        //  and include it in the constructor call
        parcel.readString()!!,
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(transactionType.name)
        // TODO: Uncomment facility OU to write OU info to parcel
//        parcel.writeString(facility.uid())
        parcel.writeString(transactionDate)
        parcel.writeString(distributedTo)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TransactionChoice> {
        override fun createFromParcel(parcel: Parcel): TransactionChoice =
            TransactionChoice(parcel)

        override fun newArray(size: Int): Array<TransactionChoice?> =
            arrayOfNulls(size)
    }

}