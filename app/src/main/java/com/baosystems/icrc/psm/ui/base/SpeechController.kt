package com.baosystems.icrc.psm.ui.base

import com.baosystems.icrc.psm.data.SpeechRecognitionState

interface SpeechController {
    fun startListening(callback: (state: SpeechRecognitionState) -> Unit)
    fun stopListening()
    fun onStateChange(state: SpeechRecognitionState)
    fun toggleState(callback: (state: SpeechRecognitionState) -> Unit)
}