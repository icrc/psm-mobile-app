package com.baosystems.icrc.psm.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppConfig(
    val program: String,
    val itemCode: String,
    val itemName: String,
    val stockOnHand: String,
    val distributedTo: String,
    val stockDistribution: String,
    val stockCorrection: String,
    val stockDiscarded: String,
) : Parcelable