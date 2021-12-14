package com.baosystems.icrc.psm.ui.reviewstock

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.google.android.material.textfield.TextInputLayout

class ReviewStockAdapter(
    private val itemWatcher: ItemWatcher<StockEntry, Long, String>,
    val appConfig: AppConfig
): ListAdapter<StockEntry, ReviewStockAdapter.StockEntryViewHolder>(DIFF_CALLBACK) {
    companion object {
        // TODO: Find a way to use a type-aware DIFF_CALLBACK for different adapters for reusability
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<StockEntry> () {
            override fun areItemsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem.item.id == newItem.item.id

            override fun areContentsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem == newItem
        }
    }

    inner class StockEntryViewHolder(
        itemView: View,
        private val watcher: ItemWatcher<StockEntry, Long, String>
    ): RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tvReviewStockItemName)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.tvReviewStockOnHandValue)
        private val tvItemQtyLayout: TextInputLayout = itemView.findViewById(R.id.tiReviewItemQty)
        private val btnRemoveItem: ImageButton = itemView.findViewById(R.id.ibRemoveStockItem)

        init {
            btnRemoveItem.setOnClickListener {
                watcher.removeItem(getItem(adapterPosition))
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, 1)
            }

            tvItemQtyLayout.editText?.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = if (s == null || TextUtils.isEmpty(s.toString())) {
                        0
                    } else { s.toString().toLong() }

                    // TODO: Add a listener to handle validation completion
                    getItem(adapterPosition)?.let { watcher.quantityChanged(it, qty, null) }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        fun bindTo(entry: StockEntry) {
            tvItemName.text = entry.item.name
            tvStockOnHand.text = entry.stockOnHand
            tvItemQtyLayout.editText?.setText(entry.qty.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockEntryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_stock_item_entry, parent, false)
        return StockEntryViewHolder(itemView, itemWatcher)
    }

    override fun onBindViewHolder(holder: StockEntryViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }
}