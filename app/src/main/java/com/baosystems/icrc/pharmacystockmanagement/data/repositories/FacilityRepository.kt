package com.baosystems.icrc.pharmacystockmanagement.data.repositories

import com.baosystems.icrc.pharmacystockmanagement.data.models.Facility

/**
 * TODO: Remove later. Temporarily used for bootstrapping
 */
class FacilityRepository(
    val facilities: ArrayList<Facility>
) {
    constructor(): this(ArrayList())

    fun addSampleFacilities() {
        facilities.addAll(getSampleFacilities())
    }

    fun getSampleFacilities() = listOf(
        Facility("x1najssKu", "Facility 1"),
        Facility("x2najssKu", "Facility 2"),
        Facility("x3najssKu", "Facility 3"),
        Facility("x4najssKu", "Facility 4"),
        Facility("x5najssKu", "Facility 5"),
    )

    fun addFacility(facility: Facility) = facilities.add(facility)
}