package com.baosystems.icrc.psm

import android.app.Application
import com.baosystems.icrc.psm.utils.ReleaseTree
import timber.log.Timber

class ApplicationController: Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(
            if (BuildConfig.DEBUG) {
                Timber.DebugTree()
            } else {
                ReleaseTree()
            }
        )
    }
}