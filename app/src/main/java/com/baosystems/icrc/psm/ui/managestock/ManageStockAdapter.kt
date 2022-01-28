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
import com.baosystems.icrc.psm.commons.Constants.CLEAR_FIELD_DELAY
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.baosystems.icrc.psm.ui.base.SpeechController
import com.baosystems.icrc.psm.ui.base.TextInputDelegate
import com.google.android.material.textfield.TextInputLayout

class ManageStockAdapter(
    private val itemWatcher: ItemWatcher<StockItem, String, String>,
    private var speechController: SpeechController?,
    val appConfig: AppConfig,
    private var voiceInputEnabled: Boolean
): PagedListAdapter<
        StockItem, ManageStockAdapter.StockItemHolder>(DIFF_CALLBACK) {
    lateinit var resources: Resources
    private val textInputDelegate: TextInputDelegate = TextInputDelegate()

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
        private val watcher: ItemWatcher<StockItem, String, String>,
    ):
        RecyclerView.ViewHolder(itemView) {

        private val tvItemName: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.stockOnHandValueTextView)
        private val etQty: TextInputLayout = itemView.findViewById(R.id.itemQtyTextField)

        init {
            addTextListener()
            addFocusListener()
        }

        private fun addFocusListener() {
            etQty.editText?.setOnFocusChangeListener { v, hasFocus ->
                textInputDelegate.focusChanged(
                    speechController, etQty, hasFocus, voiceInputEnabled, adapterPosition)
            }
        }

        private fun addTextListener() {
            etQty.editText?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = s?.toString()
                    getItem(adapterPosition)?.let {
                        textInputDelegate.textChanged(it, qty, adapterPosition, watcher)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        fun bindTo(item: StockItem) {
            tvItemName.text = item.name
            tvStockOnHand.text = watcher.getStockOnHand(item) ?: item.stockOnHand
            etQty.editText?.setText(watcher.getQuantity(item))

            var error: String? = null
            if (watcher.hasError(item)) {
                error = resources.getString(R.string.invalid_quantity)

                // Highlight the erroneous text for easy correction
                etQty.editText?.selectAll()

                // Clear the erroneous field after some time to prepare for next entry,
                // if input is via voice
                if (voiceInputEnabled)
                        textInputDelegate.clearFieldAfterDelay(
                            etQty.editText, CLEAR_FIELD_DELAY)
            }
            etQty.error = error
        }
    }

    fun voiceInputStateChanged(enabled: Boolean) {
        voiceInputEnabled = enabled

        textInputDelegate.voiceInputStateChanged(this)
    }
}