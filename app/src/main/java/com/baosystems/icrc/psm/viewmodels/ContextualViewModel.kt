package com.baosystems.icrc.psm.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel

/**
 * Custom AndroidViewModel that requires subclasses
 * to implement clean up
 */
abstract class ContextualViewModel(application: Application) :
    AndroidViewModel(application) {

    abstract fun cleanUp()
}