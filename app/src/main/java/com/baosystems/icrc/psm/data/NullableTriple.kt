package com.baosystems.icrc.psm.data

import java.io.Serializable

data class NullableTriple<A, B, C>(
    var first: A?,
    var second: B?,
    var third: C?
): Serializable {
    override fun toString() = "($first, $second, $third)"
}
