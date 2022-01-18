package com.baosystems.icrc.psm.services.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.services.SyncManager
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.utils.DateUtils
import com.baosystems.icrc.psm.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalDateTime

@HiltWorker
class SyncMetadataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val preferenceProvider: PreferenceProvider
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        var metadataSynced = false

        triggerNotification(
            R.string.app_name,
            R.string.metadata_sync_in_progress,
            R.drawable.ic_start_sync_notification
        )

        try {
            syncManager.metadataSync()
            metadataSynced = true
        } catch (e: Exception) {
            // TODO: Handle situations where the error is due to no network
            Timber.e(e)
            e.printStackTrace()
        }

        triggerNotification(
            R.string.app_name,
            R.string.metadata_sync_completed,
            R.drawable.ic_end_sync_notification
        )

        val syncDate = LocalDateTime.now().format(DateUtils.getDateTimePattern())
        preferenceProvider.setValue(Constants.LAST_METADATA_SYNC_DATE, syncDate)
        preferenceProvider.setValue(Constants.LAST_METADATA_SYNC_STATUS, metadataSynced)

        cancelNotification(Constants.SYNC_METADATA_NOTIFICATION_ID)

        if (!metadataSynced)
            return Result.failure()

        syncManager.schedulePeriodicMetadataSync()

        return Result.success()
    }

    private fun triggerNotification(title: Int, message: Int, icon: Int?) {
        NotificationHelper.triggerNotification(
            applicationContext,
            Constants.SYNC_METADATA_NOTIFICATION_ID,
            Constants.SYNC_METADATA_NOTIFICATION_CHANNEL,
            Constants.SYNC_METADATA_CHANNEL_NAME,
            applicationContext.getString(title),
            applicationContext.getString(message),
            icon
        )
    }

    private fun cancelNotification(notificationId: Int) {
        NotificationHelper.cancelNotification(applicationContext, notificationId)
    }
}