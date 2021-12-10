package com.baosystems.icrc.psm.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.services.PreferenceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor(val preferenceProvider: PreferenceProvider): ViewModel() {
    val lastSyncDate: MutableLiveData<String> = MutableLiveData()

    init {
        lastSyncDate.value = preferenceProvider.getString(Constants.LAST_DATA_SYNC_DATE)
    }
}