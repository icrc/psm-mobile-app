package com.baosystems.icrc.psm.services

import androidx.lifecycle.LiveData
import com.baosystems.icrc.psm.data.SpeechRecognitionState

interface SpeechRecognitionManager {
    fun start()
    fun restart()
    fun stop()
    fun cleanUp()
    fun getStatus(): LiveData<SpeechRecognitionState>
}