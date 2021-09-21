package com.baosystems.icrc.psm.data.repositories

import com.baosystems.icrc.psm.data.models.StockItem

/**
 * TODO: Remove later. Temporarily used for bootstrapping
 */
class StockItemRepository {
    val items: ArrayList<StockItem> = ArrayList()

    init {
        items.addAll(getSampleItems())
    }

    fun getSampleItems() = listOf(
        StockItem("x1najssKu", "Paracetamol"),
        StockItem("x2najssKu", "Panadol"),
        StockItem("x3najssKu", "B-complex"),
        StockItem("x4najssKu", "Athesunate"),
    )

    fun search(searchTerm: String): List<StockItem> {
        return items.filter { it.name.startsWith(searchTerm) }
    }

    fun lookup(uid: String): StockItem? {
        return items.find { it.name == uid }
    }
}