package com.baosystems.icrc.psm.data.models

// TODO: Remmove later, temporarily used for bootstrapping
data class Destination(
    val uid: String,
    val name: String
) {
    override fun toString() = name
}
