package com.baosystems.icrc.psm.data

import android.os.Parcel
import android.os.Parcelable
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction

class ReviewStockData(
    val transaction: Transaction,
    val items: MutableList<StockEntry>
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Transaction::class.java.classLoader)!!,
        mutableListOf<StockEntry>()
    ) {
        parcel.readTypedList(items, StockEntry)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(transaction, flags)
        parcel.writeTypedList(items)
    }

    fun removeItem(item: StockEntry) = items.remove(item)

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ReviewStockData> {
        override fun createFromParcel(parcel: Parcel): ReviewStockData {
            return ReviewStockData(parcel)
        }

        override fun newArray(size: Int): Array<ReviewStockData?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "$items"
    }
}