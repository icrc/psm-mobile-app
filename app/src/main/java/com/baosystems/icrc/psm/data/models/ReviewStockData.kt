package com.baosystems.icrc.psm.data.models

import android.os.Parcel
import android.os.Parcelable

class ReviewStockData(
    val transaction: Transaction,
    val entries: MutableList<StockEntry>
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Transaction::class.java.classLoader)!!,
        mutableListOf<StockEntry>()
    ) {
        parcel.readTypedList(entries, StockEntry.CREATOR)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(transaction, flags)
        parcel.writeTypedList(entries)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReviewStockData> {
        override fun createFromParcel(parcel: Parcel): ReviewStockData {
            return ReviewStockData(parcel)
        }

        override fun newArray(size: Int): Array<ReviewStockData?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "$entries"
    }
}