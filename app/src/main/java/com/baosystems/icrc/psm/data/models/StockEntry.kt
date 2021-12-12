package com.baosystems.icrc.psm.data.models

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.baosystems.icrc.psm.commons.Constants.NULL_NUMBER_PLACEHOLDER

data class StockEntry(
    val id: String,
    val name: String,
    val trackingData: Bundle? = null,
    var qty: Long? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readBundle(ClassLoader.getSystemClassLoader()),
        parcel.readLong()
    )

    constructor(id: String, name: String): this(id, name, null, null)
    constructor(id: String, name: String, trackingData: Bundle?): this(id, name, trackingData, null)

    override fun toString() = name

    // TODO: Ensure you check for the null value for stock on hand and qty
    //  in places where this value is used
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeBundle(trackingData)
        parcel.writeLong(qty ?: NULL_NUMBER_PLACEHOLDER)
    }

    override fun describeContents(): Int {
        return 0
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
