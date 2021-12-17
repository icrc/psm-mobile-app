package com.baosystems.icrc.psm.data.models

import android.os.Parcel
import android.os.Parcelable

data class StockEntry(
    val item: StockItem,
    var qty: String?,
    var stockOnHand: String? = null,
    var hasError: Boolean = false
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(StockItem::class.java.classLoader)!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readInt() == 1
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(item, flags)
        dest.writeString(qty)
        dest.writeString(stockOnHand)

        if (hasError)
            dest.writeInt(1)
        else
            dest.writeInt(0)
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
