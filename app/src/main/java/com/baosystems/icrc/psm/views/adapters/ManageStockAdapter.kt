package com.baosystems.icrc.psm.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.utils.AttributeHelper
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber

class StockItemHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(
        inflater.inflate(R.layout.stock_item_entry, parent, false)
    ) {
    private var tvItemName: TextView? = null
    private var tvStockOnHand: TextView? = null

    init {
        tvItemName = itemView.findViewById(R.id.itemNameTextView)
        tvStockOnHand = itemView.findViewById(R.id.stockOnHandValueTextView)
    }

    fun bindTo(item: TrackedEntityInstance) {
        // TODO: Determine if using the indices to get the item name is the best option,
        //  or comparing a preconfigured attribute uid to be compared against is better
        Timber.d("About to bind: $item")
//        tvItemName?.text = item.trackedEntityAttributeValues().
//        tvItemName?.text = "Item $pos"

//        tvItemName?.text = AttributeHelper.teiItemCode(item)
        tvItemName?.text = AttributeHelper.teiItemValueByAttributeUid(item, "MBczRWvfM46")
    }
}

class ManageStockAdapter: PagedListAdapter<TrackedEntityInstance, StockItemHolder>(DIFF_CALLBACK) {
    val TAG = "ManageStockAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        StockItemHolder(LayoutInflater.from(parent.context), parent)

    override fun onBindViewHolder(holder: StockItemHolder, position: Int) {
        // Note that the item is a placeholder if it is null
        getItem(position)?.let { item ->
            item.trackedEntityAttributeValues()?.forEach {
                Timber.d("%s - %s", it.trackedEntityAttribute(), it.value())
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
}