package com.baosystems.icrc.psm.services.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.commons.Constants.SYNC_DATA_CHANNEL_NAME
import com.baosystems.icrc.psm.commons.Constants.SYNC_DATA_NOTIFICATION_CHANNEL
import com.baosystems.icrc.psm.commons.Constants.SYNC_DATA_NOTIFICATION_ID
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.services.SyncManager
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.utils.DateUtils
import com.baosystems.icrc.psm.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalDateTime

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appConfig: AppConfig,
    private val syncManager: SyncManager,
    private val preferenceProvider: PreferenceProvider
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        var teiSynced = false

        triggerNotification(
            R.string.app_name,
            R.string.data_sync_in_progress,
            R.drawable.ic_start_sync_notification
        )

        try {
            syncManager.syncTEIs(appConfig.program)
            teiSynced = true
        } catch (e: Exception) {
            // TODO: Handle situations where the error is due to no network
            Timber.e(e)
        }

        triggerNotification(
            R.string.app_name,
            R.string.sync_completed,
            R.drawable.ic_end_sync_notification
        )

        val syncDate = LocalDateTime.now().format(DateUtils.getDateTimePattern())
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_DATE, syncDate)
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_STATUS, teiSynced)

        val syncStatus = syncManager.checkSyncStatus()
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_RESULT, syncStatus.name)

        cancelNotification(SYNC_DATA_NOTIFICATION_ID)
        syncManager.schedulePeriodicDataSync()

        return Result.success()
    }

    private fun triggerNotification(title: Int, message: Int, icon: Int?) {
        NotificationHelper.triggerNotification(
            applicationContext,
            SYNC_DATA_NOTIFICATION_ID,
            SYNC_DATA_NOTIFICATION_CHANNEL,
            SYNC_DATA_CHANNEL_NAME,
            applicationContext.getString(title),
            applicationContext.getString(message),
            icon
        )
    }

    private fun cancelNotification(notificationId: Int) {
        NotificationHelper.cancelNotification(applicationContext, notificationId)
    }
}