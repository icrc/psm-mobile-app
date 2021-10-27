package com.baosystems.icrc.psm.views.adapters

import android.annotation.SuppressLint
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
import com.baosystems.icrc.psm.utils.AttributeHelper
import com.google.android.material.textfield.TextInputLayout
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber

class ManageStockAdapter(val listener: OnItemQuantityChangedListener): PagedListAdapter<
        TrackedEntityInstance, ManageStockAdapter.StockItemHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.stock_item_entry, parent, false)
        return StockItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: StockItemHolder, position: Int) {
        // Note that the item is a placeholder if it is null
        getItem(position)?.let { item ->
            item.trackedEntityAttributeValues()?.forEach {
                Timber.d("Bound ViewHolder: %s - %s", it.trackedEntityAttribute(), it.value())
            }
            holder.bindTo(item)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackedEntityInstance>() {
            override fun areItemsTheSame(
                oldItem: TrackedEntityInstance,
                newItem: TrackedEntityInstance
            ) = oldItem.uid() == newItem.uid()

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: TrackedEntityInstance,
                newItem: TrackedEntityInstance
            ) = oldItem == newItem
        }
    }

    inner class StockItemHolder(itemView: View):
        RecyclerView.ViewHolder(itemView) {

        private val tvItemName: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.stockOnHandValueTextView)
        private val etQty: TextInputLayout = itemView.findViewById(R.id.itemQtyTextField)

        init {
            Timber.d("Created new StockItemHolder")
            etQty.editText?.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                // TODO: Also update the stock on hand value of the TEI accordingly
                // TODO: Optimize to update stock on hand after a debounce,
                //  rather than on every keystroke
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition != RecyclerView.NO_POSITION && s.toString().isNotEmpty()) {
                        listener.quantityChanged(
                            getItem(adapterPosition), s.toString().toInt())
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        fun bindTo(item: TrackedEntityInstance) {
            // TODO: Determine if using the indices to get the item name is the best option,
            //  or comparing a preconfigured attribute uid to be compared against is better
            Timber.d("About to bind: $item")
//        tvItemName?.text = item.trackedEntityAttributeValues().
//        tvItemName?.text = "Item $pos"

//        tvItemName?.text = AttributeHelper.teiItemCode(item)
            tvItemName.text = AttributeHelper.teiItemValueByAttributeUid(item, "MBczRWvfM46")
            etQty.editText?.setText(listener.itemValue(item).let { value ->
                value?.toString() ?: ""
            })
        }
    }

    interface OnItemQuantityChangedListener {
        fun quantityChanged(item: TrackedEntityInstance?, value: Int)
        fun itemValue(item: TrackedEntityInstance): Int?
    }
}