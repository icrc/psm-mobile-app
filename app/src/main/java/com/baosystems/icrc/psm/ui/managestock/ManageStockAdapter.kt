package com.baosystems.icrc.psm.ui.managestock

import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.google.android.material.textfield.TextInputLayout
import org.hisp.dhis.rules.models.RuleEffect

class ManageStockAdapter(
    private val itemWatcher: ItemWatcher<StockItem, String, String>,
    val appConfig: AppConfig
): PagedListAdapter<
        StockItem, ManageStockAdapter.StockItemHolder>(DIFF_CALLBACK) {

    lateinit var resources: Resources

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.manage_stock_item_entry, parent, false)
        resources = parent.context.resources

        return StockItemHolder(itemView, itemWatcher)
    }

    override fun onBindViewHolder(holder: StockItemHolder, position: Int) {
        // Note that the item is a placeholder if it is null
        getItem(position)?.let { item -> holder.bindTo(item) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StockItem>() {
            override fun areItemsTheSame(
                oldItem: StockItem,
                newItem: StockItem
            ) = oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: StockItem,
                newItem: StockItem
            ) = oldItem == newItem
        }
    }

    inner class StockItemHolder(
        itemView: View,
        private val watcher: ItemWatcher<StockItem, String, String>
    ):
        RecyclerView.ViewHolder(itemView) {

        private val tvItemName: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.stockOnHandValueTextView)
        private val etQty: TextInputLayout = itemView.findViewById(R.id.itemQtyTextField)

        init {
            etQty.editText?.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = s?.toString()
                    getItem(adapterPosition)?.let { stockEntry ->
                        watcher.quantityChanged(stockEntry, adapterPosition, qty, object : ItemWatcher.OnQuantityValidated {
                            override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                                watcher.updateFields(stockEntry, qty, adapterPosition, ruleEffects)
                            }
                    }) }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        fun bindTo(item: StockItem) {
            tvItemName.text = item.name
            tvStockOnHand.text = watcher.getStockOnHand(item) ?: item.stockOnHand
            etQty.editText?.setText(watcher.getQuantity(item)?.toString())

            var error: String? = null
            if (watcher.hasError(item)) {
                error = resources.getString(R.string.invalid_quantity)

                // Highlight the erroneous text for easy correction
                etQty.editText?.selectAll()
            }
            etQty.error = error
        }
    }
}