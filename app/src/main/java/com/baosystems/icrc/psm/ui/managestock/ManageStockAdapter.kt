package com.baosystems.icrc.psm.ui.managestock

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants.CLEAR_FIELD_DELAY
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.SpeechRecognitionState
import com.baosystems.icrc.psm.data.SpeechRecognitionState.Completed
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
    private var voiceInputEnabled: Boolean
): PagedListAdapter<
        StockItem, ManageStockAdapter.StockItemHolder>(DIFF_CALLBACK) {
    lateinit var resources: Resources
    private var focusPosition: Int? = null

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

            addTextListener()
            addFocusListener()
        }

        private fun addFocusListener() {
            etQty.editText?.setOnFocusChangeListener { v, hasFocus ->

                if (hasFocus)
                    focusPosition = adapterPosition

                if (hasFocus && voiceInputEnabled) {
                    etQty.endIconMode = END_ICON_CUSTOM
                    etQty.setEndIconDrawable(R.drawable.ic_mic_inactive)
                    etQty.setEndIconTintList(itemView.context.getColorStateList(R.color.mic_selector))
                    etQty.setEndIconOnClickListener { speechController.toggleState() }

                    // prevent the keyboard from showing since we're using the mic
//                    KeyboardUtils.hideKeyboard(activity, etQty?.editText?.windowToken)
                    etQty.editText?.inputType = InputType.TYPE_NULL

                    speechController.startListening {
                        if (it is Completed) {
                            etQty.editText?.setText(it.data)
                        }

                        updateMicState(it)
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

        private fun updateMicState(state: SpeechRecognitionState?) {
            state?.let {
                when (state) {
                    SpeechRecognitionState.Started ->
                        etQty.setEndIconTintList(
                            ColorStateList.valueOf(
                                ContextCompat.getColor(itemView.context, R.color.mic_active)
                            )
                        )
                    else ->
                        etQty.setEndIconTintList(
                            itemView.context.getColorStateList(R.color.mic_selector))
                }
            }
        }

        private fun addTextListener() {
            etQty.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = s?.toString()
                    getItem(adapterPosition)?.let { item ->
                        watcher.quantityChanged(
                            item,
                            adapterPosition,
                            qty,
                            object : ItemWatcher.OnQuantityValidated {
                                override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                                    watcher.updateFields(item, qty, adapterPosition, ruleEffects)
                                }
                            })
                    }
                }

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
                    clearInvalidField(etQty.editText)
            }
            etQty.error = error
        }

        private fun clearInvalidField(editText: EditText?) {
            editText?.let {
                object: CountDownTimer(CLEAR_FIELD_DELAY, CLEAR_FIELD_DELAY) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        it.setText("")
                    }
                }.start()
            }
        }
    }

    fun updateVoiceInputState(enabled: Boolean) {
        voiceInputEnabled = enabled

        // Re-render the active field to reflect the change
        focusPosition?.let {
            if (focusPosition != RecyclerView.NO_POSITION)
                notifyItemChanged(it)
        }
    }
}