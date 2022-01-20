package com.baosystems.icrc.psm.ui.base

interface SpeechController {
    fun startListening(callback: (String) -> Unit)
    fun stopListening()
    fun onResult(data: String?)
}