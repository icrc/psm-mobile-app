package com.baosystems.icrc.pharmacystockmanagement.data.models

/**
 * Temporary data model for OUs/Facilities
 */
data class Facility(
    val uid: String,
    val name: String
) {
    override fun toString() = name
}
