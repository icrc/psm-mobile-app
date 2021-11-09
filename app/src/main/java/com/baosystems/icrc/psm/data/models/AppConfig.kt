package com.baosystems.icrc.psm.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppConfig(
    val program: String,
    val itemCode: String,
    val itemValue: String,
    val stockOnHand: String
) : Parcelable
