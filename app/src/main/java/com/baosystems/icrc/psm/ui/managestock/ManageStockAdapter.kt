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
import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.google.android.material.textfield.TextInputLayout
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class ManageStockAdapter(
    private val itemWatcher: ItemWatcher<StockItem, Long, String>,
    val appConfig: AppConfig
): PagedListAdapter<
        StockItem, ManageStockAdapter.StockItemHolder>(DIFF_CALLBACK) {

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
        private val watcher: ItemWatcher<StockItem, Long, String>
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
                        null
                    } else { s.toString().toLong() }

                    getItem(adapterPosition)?.let { stockEntry ->
                        watcher.quantityChanged(stockEntry, qty, object : ItemWatcher.OnQuantityValidated {
                            override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                                Timber.d("Received Effects: %s", ruleEffects)
                                ruleEffects.forEach { ruleEffect ->
                                    if (ruleEffect.ruleAction() is RuleActionAssign &&
                                        (ruleEffect.ruleAction() as RuleActionAssign).field() == appConfig.stockOnHand) {
                                        ruleEffect.data()?.let {
//                                            updateStockOnHandColumn(it)
//                                            stockEntry.stockOnHand = it
                                            watcher.updateStockOnHand(stockEntry, it, adapterPosition)
//                                            notifyItemRangeChanged(adapterPosition, 1)
//                                            notifyItemChanged(adapterPosition)
                                        }
                                    }
                                }
                            }
                    }) }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun updateStockOnHandColumn(s: String) {
            Timber.d("Updating stock on hand column")

            etQty.editText?.setText(s)
        }

        fun bindTo(item: StockItem) {
            tvItemName.text = item.name
//            tvStockOnHand.text = item.stockOnHand
            tvStockOnHand.text = watcher.getStockOnHand(item) ?: item.stockOnHand
            etQty.editText?.setText(watcher.getQuantity(item)?.toString())
        }
    }
}