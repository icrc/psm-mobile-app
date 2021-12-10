package com.baosystems.icrc.psm.services

import androidx.lifecycle.LiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import com.baosystems.icrc.psm.commons.Constants.INITIAL_SYNC
import com.baosystems.icrc.psm.commons.Constants.INSTANT_DATA_SYNC
import com.baosystems.icrc.psm.commons.Constants.INSTANT_METADATA_SYNC
import com.baosystems.icrc.psm.commons.Constants.PERIOD_DAILY
import com.baosystems.icrc.psm.commons.Constants.PERIOD_MANUAL
import com.baosystems.icrc.psm.commons.Constants.SCHEDULED_DATA_SYNC
import com.baosystems.icrc.psm.commons.Constants.SCHEDULED_METADATA_SYNC
import com.baosystems.icrc.psm.commons.Constants.SYNC_PERIOD_DATA
import com.baosystems.icrc.psm.commons.Constants.SYNC_PERIOD_METADATA
import com.baosystems.icrc.psm.data.SyncResult
import com.baosystems.icrc.psm.data.WorkItem
import com.baosystems.icrc.psm.data.WorkType
import com.baosystems.icrc.psm.utils.toSeconds
import io.reactivex.Completable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.settings.SynchronizationSettings
import javax.inject.Inject

class SyncManagerImpl @Inject constructor(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
    private val workManagerController: WorkManagerController
): SyncManager {

    /**
     * Sync data and metadata
     */
    override fun sync() {
        workManagerController.sync(INITIAL_SYNC, INSTANT_METADATA_SYNC, INSTANT_DATA_SYNC)
    }

    override fun getSyncStatus(workName: String): LiveData<List<WorkInfo>> {
        return workManagerController.getWorkInfo(workName)
    }

    override fun getSyncSettings(): SynchronizationSettings? {
        return d2.settingModule().synchronizationSettings().blockingGet()
    }

    /**
     * Upload and download TEIs
     */
    override fun syncTEIs(program: String) {
        uploadTEIs()
            .andThen(downloadTEIs(program))
            .blockingAwait()
    }

    override fun schedulePeriodicDataSync() {
        val scheduledTimeInSecs = getSyncSettings()?.dataSync()?.toSeconds()
            ?: preferenceProvider.getInt(SYNC_PERIOD_DATA, PERIOD_DAILY)

        workManagerController.cancelUniqueWork(SCHEDULED_DATA_SYNC)

        if (scheduledTimeInSecs != PERIOD_MANUAL) {
            val work = WorkItem(
                SCHEDULED_DATA_SYNC,
                WorkType.DATA,
                null,
                scheduledTimeInSecs.toLong(),
                policy = ExistingWorkPolicy.REPLACE
            )

            workManagerController.sync(work)
        }
    }

    override fun schedulePeriodicMetadataSync() {
        val scheduledTimeInSecs = getSyncSettings()?.metadataSync()?.toSeconds()
            ?: preferenceProvider.getInt(SYNC_PERIOD_METADATA, PERIOD_DAILY)

        workManagerController.cancelUniqueWork(SCHEDULED_METADATA_SYNC)

        if (scheduledTimeInSecs != PERIOD_MANUAL) {
            val work = WorkItem(
                SCHEDULED_METADATA_SYNC,
                WorkType.METADATA,
                null,
                scheduledTimeInSecs.toLong(),
                policy = ExistingWorkPolicy.REPLACE
            )

            workManagerController.sync(work)
        }
    }

    private fun downloadTEIs(program: String): Completable {
        return Completable.fromObservable(
            d2.trackedEntityModule()
                .trackedEntityInstanceDownloader()
                .byProgramUid(program)
                .limitByOrgunit(true)
                .limitByProgram(true)
                .download()
        )
    }

    private fun uploadTEIs(): Completable {
        return Completable.fromObservable(
            d2.trackedEntityModule().trackedEntityInstances().upload()
        )
    }

    override fun checkSyncStatus(): SyncResult {
        val teisSynced = d2.trackedEntityModule()
            .trackedEntityInstances()
            .byAggregatedSyncState()
            .notIn(State.SYNCED, State.RELATIONSHIP)
            .blockingGet()
            .isEmpty()

        if (teisSynced)
            return SyncResult.SYNCED

        val outstandingTEIsToPostOrUpdate = d2.trackedEntityModule()
            .trackedEntityInstances()
            .byAggregatedSyncState().
            `in`(State.TO_POST, State.TO_UPDATE)
            .blockingGet().isNotEmpty()

        if (outstandingTEIsToPostOrUpdate)
            return SyncResult.INCOMPLETE

        return SyncResult.ERRORED
    }

    override fun metadataSync() {
        Completable.fromObservable(d2.metadataModule().download()).blockingAwait()
    }

//    override fun dataSync(program: String): Observable<D2Progress> {
//        return Observable.defer{
//            d2.trackedEntityModule()
//                .trackedEntityInstanceDownloader()
//                .byProgramUid(program)
//                .limitByOrgunit(true)
//                .limitByProgram(true)
//                .download()
//        }
//    }
//
//    override fun upload(): Observable<D2Progress>? {
//        return d2.eventModule().events().upload()
//    }
}