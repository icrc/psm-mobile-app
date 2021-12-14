package com.baosystems.icrc.psm.data.models

import android.os.Parcel
import android.os.Parcelable

data class StockEntry(
    val item: StockItem,
    var qty: Long,
    var stockOnHand: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(StockItem::class.java.classLoader)!!,
        parcel.readLong(),
        parcel.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(item, flags)
        dest.writeLong(qty)
        dest.writeString(stockOnHand)
    }

    companion object CREATOR : Parcelable.Creator<StockEntry> {
        override fun createFromParcel(parcel: Parcel): StockEntry {
            return StockEntry(parcel)
        }

        override fun newArray(size: Int): Array<StockEntry?> {
            return arrayOfNulls(size)
        }
    }
}
