package com.baosystems.icrc.pharmacystockmanagement.data.repositories

import com.baosystems.icrc.pharmacystockmanagement.data.models.Destination

/**
 * TODO: Remove later. Temporarily used for bootstrapping
 */
class DestinationRepository(
    val destinations: ArrayList<Destination>
) {
    constructor(): this(ArrayList())

    fun addSampleDestinations() {
        destinations.addAll(getSampleDestinations())
    }

    fun getSampleDestinations() = listOf(
        Destination("d001", "Destination 1"),
        Destination("d002", "Destination 2"),
        Destination("d003", "Destination 3"),
        Destination("d004", "Destination 4"),
        Destination("d005", "Destination 5"),
    )

    fun addDestination(destination: Destination) = destinations.add(destination)
}