package com.baosystems.icrc.psm.data.repositories

import com.baosystems.icrc.psm.data.models.Destination

/**
 * TODO: Remove later. Temporarily used for bootstrapping
 */
class DestinationRepository {
    val destinations: ArrayList<Destination> = ArrayList()

    init {
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