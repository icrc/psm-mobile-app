package com.baosystems.icrc.psm.data

import android.os.Parcelable
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewStockData(
    val transaction: Transaction,
    val items: List<StockEntry>
): Parcelable