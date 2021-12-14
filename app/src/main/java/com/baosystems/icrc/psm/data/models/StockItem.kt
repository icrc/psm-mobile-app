package com.baosystems.icrc.psm.data.models

import android.os.Parcel
import android.os.Parcelable

data class StockItem(
    val id: String,
    val name: String,
    var stockOnHand: String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()
    )

    constructor(id: String, name: String): this(id, name, null)

    override fun toString() = name

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(stockOnHand ?: "")
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<StockItem> {
        override fun createFromParcel(parcel: Parcel): StockItem {
            return StockItem(parcel)
        }

        override fun newArray(size: Int): Array<StockItem?> {
            return arrayOfNulls(size)
        }
    }
}
