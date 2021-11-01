package com.baosystems.icrc.psm.views.adapters

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
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber

class ReviewStockAdapter(
    entries: List<StockEntry>,
    private val removeItemListener: View.OnClickListener,
    private val quantityChangeListener: TextWatcher
): ListAdapter<StockEntry, ReviewStockAdapter.StockItemHolder>(DIFF_CALLBACK) {

    init {
        Timber.i("Entries: ", entries)
        submitList(entries)
    }

    companion object {
        // TODO: Find a way to use a type-aware DIFF_CALLBACK for different adapters for reusability
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<StockEntry> () {
            override fun areItemsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem == newItem
        }
    }

    inner class StockItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tvReviewStockItemName)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.tvReviewStockOnHandValue)
        private val tvItemQtyLayout: TextInputLayout = itemView.findViewById(R.id.tiReviewItemQty)
        private val btnRemoveItem: ImageButton = itemView.findViewById(R.id.ibRemoveStockItem)

        init {
//            btnRemoveItem.setOnClickListener(removeItemListener)
            btnRemoveItem.setOnClickListener {
                Timber.d("Tried deleting %s", getItem(adapterPosition))
            }

            tvItemQtyLayout.editText?.addTextChangedListener(quantityChangeListener)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_stock_item_entry, parent, false)
        return StockItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: StockItemHolder, position: Int) {
        holder.bindTo(getItem(position))
    }
}