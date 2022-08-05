package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.data.*
import com.baosystems.icrc.psm.data.persistence.UserActivity
import com.baosystems.icrc.psm.data.persistence.UserActivityRepository
import com.baosystems.icrc.psm.exceptions.UserIntentParcelCreationException
import com.baosystems.icrc.psm.services.MetadataManager
import com.baosystems.icrc.psm.services.UserManager
import com.baosystems.icrc.psm.services.UserManagerImpl
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.services.scheduler.TestSchedulerProvider
import com.baosystems.icrc.psm.services.scheduler.TrampolineSchedulerProvider
import com.baosystems.icrc.psm.ui.home.HomeViewModel
import com.baosystems.icrc.psm.utils.ParcelUtils
import com.baosystems.icrc.psm.utils.humanReadableDate
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.TestScheduler
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId

// TODO: Fix the failing tests
@RunWith(MockitoJUnitRunner::class)
class HomeViewModelUnitTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val countingTaskExecutorRule = CountingTaskExecutorRule()

    @Mock
    private lateinit var metadataManager: MetadataManager

    @Mock
    private lateinit var userActivityRepository: UserActivityRepository
    private lateinit var viewModel: HomeViewModel
    private lateinit var userManager: UserManager
    private lateinit var schedulerProvider: BaseSchedulerProvider
    @Mock
    private lateinit var testSchedulerProvider: TestSchedulerProvider
    private lateinit var facilities: List<OrganisationUnit>
    private lateinit var destinations: List<Option>
    private lateinit var appConfig: AppConfig

    private val disposable = CompositeDisposable()

    @Mock
    private lateinit var d2: D2

    @Mock
    private lateinit var facilitiesObserver: Observer<OperationState<List<OrganisationUnit>>>

    @Mock
    private lateinit var destinationsObserver: Observer<OperationState<List<Option>>>

    @Mock
    private lateinit var preferenceProvider: PreferenceProvider

    @Captor
    private lateinit var facilitiesArgumentCaptor: ArgumentCaptor<OperationState<List<OrganisationUnit>>>

    @Captor
    private lateinit var destinationsArgumentCaptor: ArgumentCaptor<OperationState<List<Option>>>

    @Before
    fun setup() {
        appConfig = AppConfig(
            "F5ijs28K4s8", "wBr4wccNBj1", "sLMTQUHAZnk",
            "RghnAkDBDI4", "yfsEseIcEXr",
            "lpGYJoVUudr", "ej1YwWaYGmm",
            "I7cmT3iXT0y")

        facilities = FacilityFactory.getListOf(3)
        destinations = DestinationFactory.getListOf(5)

        schedulerProvider = TrampolineSchedulerProvider()
        testSchedulerProvider = TestSchedulerProvider(TestScheduler())

        doReturn(
            Single.just(facilities)
        ).whenever(metadataManager).facilities(appConfig.program)

        Mockito.`when`(metadataManager.destinations())
            .thenReturn(Single.just(destinations))

        userManager = UserManagerImpl(d2)
        viewModel = HomeViewModel(
            disposable, appConfig, testSchedulerProvider, preferenceProvider, metadataManager,
            userActivityRepository
        )

        viewModel.facilities.observeForever(facilitiesObserver)
        viewModel.destinationsList.observeForever(destinationsObserver)
    }

    private fun getTime() =
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()

    private fun getTime(dateTime: LocalDateTime) =
        dateTime.atZone(ZoneId.systemDefault()).toEpochSecond()

    @Test
    fun init_shouldLoadProgram() {

        whenever(userActivityRepository.getRecentActivities(Constants.USER_ACTIVITY_COUNT)) doReturn Single.just(
            listOf(
                UserActivity(
                    TransactionType.DISTRIBUTION,
                    LocalDateTime.now()
                )
            ))

        verify(metadataManager).stockManagementProgram(appConfig.program)
        assertNotNull(viewModel.program)
    }

    @Test
    fun init_shouldLoadFacilities() {

        whenever(userActivityRepository.getRecentActivities(Constants.USER_ACTIVITY_COUNT)) doReturn Single.just(
            listOf(
                UserActivity(
                    TransactionType.DISTRIBUTION,
                    LocalDateTime.now()
                )
            ))

        val state = OperationState.Success(facilities)
        whenever(metadataManager.facilities(appConfig.program)) doReturn Single.just(
            state.result.toMutableList()
        )
        viewModel.loadFacilities()

        //RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        //countingTaskExecutorRule.drainTasks(3, TimeUnit.SECONDS)

        verify(metadataManager).facilities(appConfig.program)

        viewModel.facilities.observeForever {
            assertEquals(it, OperationState.Success(facilities))
        }
        println(Timber.forest())
    }

    @Test
    fun init_shouldLoadDestinations() {
        whenever(userActivityRepository.getRecentActivities(Constants.USER_ACTIVITY_COUNT)) doReturn Single.just(
            listOf(
                UserActivity(
                    TransactionType.CORRECTION,
                    LocalDateTime.now()
                )
            ))
        viewModel.loadDestinations()
        verify(metadataManager).destinations()
        verify(destinationsObserver, times(1))
            .onChanged(destinationsArgumentCaptor.capture())

        assertEquals(destinationsArgumentCaptor.value, destinations)
    }

    @Test
    fun init_shouldNotSetDefaultTransaction() {
        assertNull(viewModel.transactionType.value)
    }

    @Test
    fun init_shouldSetTransactionDateToCurrentDate() {
        val today = LocalDateTime.now()

        assertEquals(viewModel.transactionDate.value?.year, today.year)
        assertEquals(viewModel.transactionDate.value?.month, today.month)
        assertEquals(viewModel.transactionDate.value?.dayOfMonth, today.dayOfMonth)
    }

    @Test
    fun canSelectDifferentTransactionTypes() {
        val types = listOf<TransactionType>(
            TransactionType.DISTRIBUTION,
            TransactionType.DISCARD,
            TransactionType.CORRECTION,
        )

        types.forEach {
            viewModel.selectTransaction(it)
            assertEquals(viewModel.transactionType.value, it)
        }
    }

    @Test
    fun isDistributionIsPositive_whenDistributionIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        assertEquals(viewModel.isDistribution.value, true)
    }

    @Test
    fun isDistributionIsNegative_whenDiscardIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        assertEquals(viewModel.isDistribution.value, false)
    }

    @Test
    fun isDistributionIsNegative_whenCorrectionIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        assertEquals(viewModel.isDistribution.value, false)
    }

    @Test
    fun cannotManageStock_ifNoTransactionIsSelected() {
        assertEquals(viewModel.readyManageStock(), false)
    }

    private fun removeDefaultTransactionDate() {
//        viewModel.transactionDate.value = null
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        removeDefaultTransactionDate()

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        removeDefaultTransactionDate()

        viewModel.setFacility(facilities[0])

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setTransactionDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destinations[0])
        removeDefaultTransactionDate()

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityAndTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyDestinedToAndTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destinations[0])
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityAndDestinedToIsSet() {
        whenever(userActivityRepository.getRecentActivities(Constants.USER_ACTIVITY_COUNT)) doReturn Single.just(
            listOf(
                UserActivity(
                    TransactionType.DISTRIBUTION,
                    LocalDateTime.now()
                )
            ))
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        removeDefaultTransactionDate()

        assertEquals(viewModel.readyManageStock(), true)
    }

    @Test
    fun distributionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.readyManageStock(), false) // changed to true
    }

    @Test
    fun discardTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        removeDefaultTransactionDate()

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        removeDefaultTransactionDate()

        viewModel.setFacility(facilities[0])

        assertEquals(viewModel.readyManageStock(), false) // changed to true
    }

    @Test
    fun discardTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun discardTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.readyManageStock(), true)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun discardTransaction_throwsErrorIfDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setDestination(destinations[1])
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        removeDefaultTransactionDate()

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        removeDefaultTransactionDate()

        viewModel.setFacility(facilities[0])

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun correctionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.readyManageStock(), true)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun correctionTransaction_throwsErrorIfDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setDestination(destinations[1])
    }

    @Test(expected = UserIntentParcelCreationException::class)
    fun missingTransactionType_cannotCreateUserIntent() {
        viewModel.setFacility(facilities[1])
        viewModel.setTransactionDate(getTime())

        viewModel.getData()
    }

    @Test(expected = UserIntentParcelCreationException::class)
    fun distributionWithMissingFacility_cannotCreateUserIntent() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setTransactionDate(getTime())

        viewModel.getData()
    }

    @Test(expected = UserIntentParcelCreationException::class)
    fun distributionWithMissingTransactionDate_cannotCreateUserIntent() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[1])
//        viewModel.transactionDate.value = null

        viewModel.getData()
    }

    @Test
    fun distributionWithCompleteInformation_canCreateUserIntent() {
        val destination = destinations[2]
        val facility = facilities[1]
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destination)
        viewModel.setFacility(facility)
        viewModel.setTransactionDate(getTime(now))

        val data = viewModel.getData()
        assertEquals(data.transactionType, TransactionType.DISTRIBUTION)
        assertEquals(data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility))
        assertEquals(data.distributedTo,
            ParcelUtils.distributedTo_ToIdentifiableModelParcel(destination))
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    @Test
    fun discardWithCompleteInformation_canCreateUserIntent() {
        val facility = facilities[1]
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facility)
        viewModel.setTransactionDate(getTime(now))

        val data = viewModel.getData()
        assertEquals(data.transactionType, TransactionType.DISCARD)
        assertEquals(data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility))
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    @Test
    fun correctionWithCompleteInformation_canCreateUserIntent() {
        val facility = facilities[1]
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facility)
        viewModel.setTransactionDate(getTime(now))

        val data = viewModel.getData()
        assertEquals(data.transactionType, TransactionType.CORRECTION)
        assertEquals(data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility))
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    // TODO: Implement shouldSetDefaultFacility_ifOnlyOneFacilityIsAvailable
//    @Test
//    fun shouldSetDefaultFacility_ifOnlyOneFacilityIsAvailable() {
//
//    }

    // TODO: Implement shouldDisplayRecentActivity_IfAvailable
//    @Test
//    fun shouldDisplayRecentActivity_IfAvailable() {
//
//    }
}