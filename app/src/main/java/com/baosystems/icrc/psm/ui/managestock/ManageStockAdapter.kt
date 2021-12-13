package com.baosystems.icrc.psm.ui.managestock

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextUtils
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
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.google.android.material.textfield.TextInputLayout
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class ManageStockAdapter(
    private val itemWatcher: ItemWatcher<StockEntry, Long>,
    val appConfig: AppConfig
): PagedListAdapter<
        StockEntry, ManageStockAdapter.StockItemHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.manage_stock_item_entry, parent, false)
        return StockItemHolder(itemView, itemWatcher)
    }

    override fun onBindViewHolder(holder: StockItemHolder, position: Int) {
        // Note that the item is a placeholder if it is null
        getItem(position)?.let { item -> holder.bindTo(item) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StockEntry>() {
            override fun areItemsTheSame(
                oldItem: StockEntry,
                newItem: StockEntry
            ) = oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: StockEntry,
                newItem: StockEntry
            ) = oldItem == newItem
        }
    }

    inner class StockItemHolder(
        itemView: View,
        private val watcher: ItemWatcher<StockEntry, Long>
    ):
        RecyclerView.ViewHolder(itemView) {

        private val tvItemName: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.stockOnHandValueTextView)
        private val etQty: TextInputLayout = itemView.findViewById(R.id.itemQtyTextField)

        init {
            etQty.editText?.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int) {}

                // TODO: Optimize to update stock on hand after a debounce,
                //  rather than on every keystroke
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = if (s == null || TextUtils.isEmpty(s.toString())) {
                        0
                    } else { s.toString().toLong() }

                    getItem(adapterPosition)?.let { watcher.quantityChanged(it, qty, object : ItemWatcher.OnQuantityValidated {
                        override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                            Timber.d("Received Effects: %s", ruleEffects)
                        }
                    }) }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        fun bindTo(item: StockEntry) {
            val stockOnHandUid = appConfig.stockOnHand

            tvItemName.text = item.name
            tvStockOnHand.text = item.trackingData?.getString(stockOnHandUid)
            etQty.editText?.setText(watcher.getValue(item).let { value ->
                value?.toString() ?: ""
            })
        }
    }
}