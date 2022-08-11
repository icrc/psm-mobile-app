package com.baosystems.icrc.psm.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baosystems.icrc.psm.BuildConfig
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.RowAction
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.rules.RuleValidationHelper
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import java.util.Date
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor(
    private val preferenceProvider: PreferenceProvider,
    private val schedulerProvider: BaseSchedulerProvider
) : ViewModel() {
    val lastSyncDate: LiveData<String> = MutableLiveData(
        preferenceProvider.getString(Constants.LAST_DATA_SYNC_DATE)
    )
    private val _showGuide: MutableLiveData<Boolean> = MutableLiveData(false)
    val showGuide: LiveData<Boolean>
        get() = _showGuide

    val appVersion: LiveData<String> = MutableLiveData(getAppVersion())

    /**
     * Evaluates the quantity assigned to the StockItem
     *
     * @param action The row action that comprises the item, adapter position, quantity and
     * callback invoked when the validation completes
     */
    fun evaluate(
        ruleValidationHelper: RuleValidationHelper,
        action: RowAction,
        program: String,
        transaction: Transaction,
        date: Date,
        appConfig: AppConfig
    ): Disposable {

        return ruleValidationHelper.evaluate(
            entry = action.entry,
            eventDate = date,
            program = program,
            transaction = transaction,
            appConfig = appConfig
        )
            .doOnError { it.printStackTrace() }
            .observeOn(schedulerProvider.io())
            .subscribeOn(schedulerProvider.ui())
            .subscribe { ruleEffects ->
                action.callback?.validationCompleted(ruleEffects)
            }
    }

    fun toggleGuideDisplay() {
        _showGuide.value = if (_showGuide.value == null) {
            true
        } else {
            !_showGuide.value!!
        }
    }

    fun isVoiceInputEnabled(prefKey: String) = preferenceProvider.getBoolean(prefKey, false)

    private fun getAppVersion() = "v" + BuildConfig.VERSION_NAME
}