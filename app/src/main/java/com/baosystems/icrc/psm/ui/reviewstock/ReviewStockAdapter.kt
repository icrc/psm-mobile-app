package com.baosystems.icrc.psm.ui.reviewstock

import android.content.Context
import android.content.res.Resources
import android.text.Editable
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
import com.baosystems.icrc.psm.ui.base.SpeechController
import com.baosystems.icrc.psm.ui.base.TextInputDelegate
import com.baosystems.icrc.psm.utils.ActivityManager
import com.google.android.material.textfield.TextInputLayout

class ReviewStockAdapter(
    private val itemWatcher: ItemWatcher<StockEntry, String, String>,
    private val speechController: SpeechController?,
    val appConfig: AppConfig,
    private var voiceInputEnabled: Boolean
): ListAdapter<StockEntry, ReviewStockAdapter.StockEntryViewHolder>(DIFF_CALLBACK) {
    private lateinit var context: Context
    private lateinit var resources: Resources
    private val textInputDelegate: TextInputDelegate = TextInputDelegate()

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
        private val watcher: ItemWatcher<StockEntry, String, String>
    ): RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.review_stock_item_name_text_view)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.review_stock_on_hand_value_text_view)
        private val tvItemQtyLayout: TextInputLayout = itemView.findViewById(R.id.review_item_qty_layout)
        private val btnRemoveItem: ImageButton = itemView.findViewById(R.id.remove_stock_item_image_button)

        init {
            btnRemoveItem.setOnClickListener {
                ActivityManager.showDialog(
                    context,
                    R.string.confirmation,
                    context.getString(R.string.remove_confirmation_message,
                        getItem(adapterPosition).item.name
                    )
                ) {
                    watcher.removeItem(getItem(adapterPosition))
                }
            }

            addTextListener()
            addFocusListener()
        }

        fun bindTo(entry: StockEntry) {
            tvItemName.text = entry.item.name
            tvStockOnHand.text = entry.stockOnHand

            val qty = watcher.getQuantity(entry)
            tvItemQtyLayout.editText?.setText(qty)

            var error: String? = null
            // A reviewed quantity should not be left empty
            if (watcher.hasError(entry) || qty.isNullOrEmpty()) {
                error = resources.getString(R.string.invalid_quantity)

                // Highlight the erroneous text for easy correction
                if (!qty.isNullOrEmpty())
                    tvItemQtyLayout.editText?.selectAll()
            }
            tvItemQtyLayout.error = error
        }

        private fun addTextListener() {
            tvItemQtyLayout.editText?.addTextChangedListener(object: TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = s?.toString()
                    getItem(adapterPosition)?.let {
                        textInputDelegate.textChanged(it, qty, adapterPosition, watcher)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        private fun addFocusListener() {
            tvItemQtyLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
                textInputDelegate.focusChanged(
                    speechController, tvItemQtyLayout, hasFocus, voiceInputEnabled, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockEntryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_stock_item_entry, parent, false)
        resources = parent.context.resources
        context = parent.context

        return StockEntryViewHolder(itemView, itemWatcher)
    }

    override fun onBindViewHolder(holder: StockEntryViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    fun voiceInputStateChanged(status: Boolean) {
        voiceInputEnabled = status
        textInputDelegate.voiceInputStateChanged(this)
    }
}