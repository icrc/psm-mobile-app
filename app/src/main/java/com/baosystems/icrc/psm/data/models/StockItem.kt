package com.baosystems.icrc.psm.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StockItem(
    val id: String,
    val name: String,
    var stockOnHand: String?
): Parcelable