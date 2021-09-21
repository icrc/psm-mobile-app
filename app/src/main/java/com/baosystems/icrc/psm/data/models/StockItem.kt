package com.baosystems.icrc.psm.data.models

/**
 * Temporary data model for OUs/Facilities
 */
data class StockItem(
    val uid: String,
    val name: String
) {
    override fun toString() = name
}
