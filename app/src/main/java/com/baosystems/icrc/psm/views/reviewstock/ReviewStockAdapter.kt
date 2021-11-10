package com.baosystems.icrc.psm.views.reviewstock

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
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.views.base.ItemWatcher
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber

class ReviewStockAdapter(
    private val itemWatcher: ItemWatcher<StockEntry, Long>
): ListAdapter<StockEntry, ReviewStockAdapter.StockItemViewHolder>(DIFF_CALLBACK) {
    companion object {
        // TODO: Find a way to use a type-aware DIFF_CALLBACK for different adapters for reusability
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<StockEntry> () {
            override fun areItemsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem == newItem
        }
    }

    inner class StockItemViewHolder(
        itemView: View,
        private val watcher: ItemWatcher<StockEntry, Long>
    ): RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tvReviewStockItemName)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.tvReviewStockOnHandValue)
        private val tvItemQtyLayout: TextInputLayout = itemView.findViewById(R.id.tiReviewItemQty)
        private val btnRemoveItem: ImageButton = itemView.findViewById(R.id.ibRemoveStockItem)

        init {
            btnRemoveItem.setOnClickListener {
                watcher.removeItem(getItem(adapterPosition))
                notifyItemRemoved(adapterPosition)
            }

            tvItemQtyLayout.editText?.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s == null || TextUtils.isEmpty(s.toString()) ||
                        adapterPosition == RecyclerView.NO_POSITION) return

                    getItem(adapterPosition)?.let {
                        watcher.quantityChanged(it, s.toString().toLong())
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })

            Timber.d("Initialized new StockItemHolder with listeners")
        }

        fun bindTo(stockEntry: StockEntry) {
            Timber.d("StockEntry: %s, Qty: %d", stockEntry, stockEntry.qty)
            tvItemName.text = stockEntry.name
            // TODO: Bind the stock on hand value
            tvStockOnHand.text = ""
            tvItemQtyLayout.editText?.setText(stockEntry.qty.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_stock_item_entry, parent, false)
        return StockItemViewHolder(itemView, itemWatcher)
    }

    override fun onBindViewHolder(holder: StockItemViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }
}