package com.baosystems.icrc.psm.ui.base

import com.baosystems.icrc.psm.data.SpeechRecognitionState

class SpeechControllerImpl(private val viewModel: SpeechRecognitionAwareViewModel): SpeechController {
    private var callback: Function1<SpeechRecognitionState, Unit>? = null

    override fun onStateChange(state: SpeechRecognitionState) {
        if (callback != null) callback!!.invoke(state)
    }

    override fun startListening(callback: (state: SpeechRecognitionState) -> Unit) {
        this.callback = callback

        viewModel.startListening()
    }

    override fun stopListening() {
        viewModel.stopListening()
    }

    override fun toggleState() {
        viewModel.toggleSpeechRecognitionState()
    }
}