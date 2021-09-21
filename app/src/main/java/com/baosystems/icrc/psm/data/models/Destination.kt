package com.baosystems.icrc.psm.data.models

data class Destination(
    val uid: String,
    val name: String
) {
    override fun toString() = name
}
