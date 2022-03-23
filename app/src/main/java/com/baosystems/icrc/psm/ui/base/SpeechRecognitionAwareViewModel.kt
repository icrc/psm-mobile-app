package com.baosystems.icrc.psm.ui.base

import com.baosystems.icrc.psm.data.SpeechRecognitionState
import com.baosystems.icrc.psm.services.SpeechRecognitionManager
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import javax.inject.Inject

open class SpeechRecognitionAwareViewModel @Inject constructor(
    preferenceProvider: PreferenceProvider,
    schedulerProvider: BaseSchedulerProvider,
    private val speechRecognitionManager: SpeechRecognitionManager
): BaseViewModel(preferenceProvider, schedulerProvider) {
    fun startListening() {
        speechRecognitionManager.start()
    }

    fun stopListening() {
        speechRecognitionManager.stop()
    }

    fun getSpeechStatus() = speechRecognitionManager.getStatus()

    fun toggleSpeechRecognitionState() {
        val state = getSpeechStatus().value ?: return

        if (state == SpeechRecognitionState.Started) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun resetSpeechStatus() {
        speechRecognitionManager.resetStatus()
    }
}