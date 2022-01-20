package com.baosystems.icrc.psm.ui.managestock

import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Editable
import android.text.InputType
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
import com.baosystems.icrc.psm.ui.base.SpeechController
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class ManageStockAdapter(
    private val itemWatcher: ItemWatcher<StockItem, String, String>,
    private val speechController: SpeechController,
    val appConfig: AppConfig,
    var voiceInputEnabled: Boolean
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
        const val CLEAR_ICON = 0

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
            Timber.d("Voice input enabled: %s", voiceInputEnabled)

            etQty.editText?.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = s?.toString()
                    getItem(adapterPosition)?.let { item ->
                        watcher.quantityChanged(item, adapterPosition, qty, object : ItemWatcher.OnQuantityValidated {
                            override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                                watcher.updateFields(item, qty, adapterPosition, ruleEffects)
                            }
                    }) }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            etQty.editText?.setOnFocusChangeListener { v, hasFocus ->
                Timber.d("Item: %s, has focus: %s, voice input enabled: %s",
                    getItem(adapterPosition)?.name, hasFocus, voiceInputEnabled)

                if (hasFocus && voiceInputEnabled) {
                    etQty.endIconMode = END_ICON_CUSTOM
                    etQty.setEndIconDrawable(R.drawable.ic_mic_active)
                    etQty.setEndIconOnClickListener {
                        Timber.d("Mic activated")

                        // TODO: Stop the mic if already started, otherwise restart it
                    }

                    // prevent the keyboard from showing since we're using the mic
//                    KeyboardUtils.hideKeyboard(activity, etQty?.editText?.windowToken)
                    etQty.editText?.inputType = InputType.TYPE_NULL

                    speechController.startListening { result ->
                        etQty.editText?.setText(result)
                    }

                } else {
                    etQty.endIconMode = END_ICON_NONE
                    etQty.setEndIconDrawable(Companion.CLEAR_ICON)
                    etQty.setEndIconOnClickListener(null)

                    // reset the input type back to default
                    etQty.editText?.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED
                }
            }
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
            }
            etQty.error = error
        }
    }
}