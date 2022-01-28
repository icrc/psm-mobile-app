package com.baosystems.icrc.psm.ui.base

import android.content.res.ColorStateList
import android.text.InputType
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants.CLEAR_ICON
import com.baosystems.icrc.psm.data.SpeechRecognitionState
import com.google.android.material.textfield.TextInputLayout

class TextInputLayoutFocusListener(
    private val textInputLayout: TextInputLayout,
    private val viewHolder: RecyclerView.ViewHolder,
    private val speechController: SpeechController,
    var voiceInputEnabled: Boolean
): View.OnFocusChangeListener {
    private var focusPosition: Int? = null

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus)
            focusPosition = viewHolder.adapterPosition

        if (hasFocus && voiceInputEnabled) {
            textInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputLayout.setEndIconDrawable(R.drawable.ic_mic_inactive)
            textInputLayout.setEndIconTintList(textInputLayout.context.getColorStateList(R.color.mic_selector))
            textInputLayout.setEndIconOnClickListener { speechController.toggleState() }

            // prevent the keyboard from showing since we're using the mic
//                    KeyboardUtils.hideKeyboard(activity, etQty?.editText?.windowToken)
            textInputLayout.editText?.inputType = InputType.TYPE_NULL

            speechController.startListening {
                if (it is SpeechRecognitionState.Completed) {
                    textInputLayout.editText?.setText(it.data)
                }

                textInputLayout.context
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

    fun updateVoiceInputState(adapter: RecyclerView.Adapter<*>, enabled: Boolean) {
        voiceInputEnabled = enabled

        // Re-render the active field to reflect the change
        focusPosition?.let {
            if (focusPosition != RecyclerView.NO_POSITION)
                adapter.notifyItemChanged(it)
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
}