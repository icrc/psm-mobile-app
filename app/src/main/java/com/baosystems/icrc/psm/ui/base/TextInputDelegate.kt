package com.baosystems.icrc.psm.ui.base

import android.content.res.ColorStateList
import android.os.CountDownTimer
import android.text.InputType
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants.CLEAR_ICON
import com.baosystems.icrc.psm.data.SpeechRecognitionState
import com.google.android.material.textfield.TextInputLayout
import org.hisp.dhis.rules.models.RuleEffect

class TextInputDelegate {
    private var focusPosition: Int? = null

    fun <A, B, C> textChanged(item: A, qty: B?, position: Int, itemWatcher: ItemWatcher<A, B, C>) {
        item?.let {
            itemWatcher.quantityChanged(
                it,
                position,
                qty,
                object : ItemWatcher.OnQuantityValidated {
                    override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                        itemWatcher.updateFields(it, qty, position, ruleEffects)
                    }
                })
        }
    }

    fun focusChanged(speechController: SpeechController?, textInputLayout: TextInputLayout, hasFocus: Boolean,
                     voiceInputEnabled: Boolean, position: Int) {
        if (hasFocus)
            focusPosition = position

        if (hasFocus && voiceInputEnabled) {
            textInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputLayout.setEndIconDrawable(R.drawable.ic_mic_inactive)
            textInputLayout.setEndIconTintList(textInputLayout.context.getColorStateList(R.color.mic_selector))
            textInputLayout.setEndIconOnClickListener { speechController?.toggleState() }

            // prevent the keyboard from showing since we're using the mic
//                    KeyboardUtils.hideKeyboard(activity, etQty?.editText?.windowToken)
            textInputLayout.editText?.inputType = InputType.TYPE_NULL

            speechController?.startListening {
                if (it is SpeechRecognitionState.Completed) {
                    textInputLayout.editText?.setText(it.data)
                }

                updateMicState(textInputLayout, it)
            }
        } else {
            textInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
            textInputLayout.setEndIconDrawable(CLEAR_ICON)
            textInputLayout.setEndIconOnClickListener(null)

            // reset the input type back to default
            textInputLayout.editText?.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED
        }
    }

    private fun updateMicState(textInputLayout: TextInputLayout, state: SpeechRecognitionState?) {
        state?.let {
            when (state) {
                SpeechRecognitionState.Started ->
                    textInputLayout.setEndIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(textInputLayout.context, R.color.mic_active)
                        )
                    )
                else ->
                    textInputLayout.setEndIconTintList(
                        textInputLayout.context.getColorStateList(R.color.mic_selector))
            }
        }
    }

    fun clearFieldAfterDelay(editText: EditText?, delay: Long) {
        editText?.let {
            object: CountDownTimer(delay, delay) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    it.setText("")
                }
            }.start()
        }
    }

    /**
     * Re-render the active field to reflect voice input settings change
     */
    fun voiceInputStateChanged(adapter: RecyclerView.Adapter<*>) {
        focusPosition?.let {
            if (focusPosition != RecyclerView.NO_POSITION)
                adapter.notifyItemChanged(it)
        }
    }
}