package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.baosystems.icrc.psm.R
import com.baosystems.icrc.psm.commons.Constants
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.DestinationFactory
import com.baosystems.icrc.psm.data.FacilityFactory
import com.baosystems.icrc.psm.data.OperationState
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.persistence.UserActivity
import com.baosystems.icrc.psm.data.persistence.UserActivityDao
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
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(MockitoJUnitRunner::class)
class HomeViewModelUnitTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val countingTaskExecutorRule = CountingTaskExecutorRule()

    @Mock
    private lateinit var metadataManager: MetadataManager

    @Mock
    private lateinit var userActivityDao: UserActivityDao

    private lateinit var userActivityRepository: UserActivityRepository
    private lateinit var viewModel: HomeViewModel
    private lateinit var userManager: UserManager
    private lateinit var schedulerProvider: BaseSchedulerProvider

    @Mock
    private lateinit var testSchedulerProvider: TestSchedulerProvider
    private lateinit var facilities: List<OrganisationUnit>
    private lateinit var destinations: List<Option>
    private lateinit var recentActivities: List<UserActivity>
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
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        appConfig = AppConfig(
            "F5ijs28K4s8", "wBr4wccNBj1", "sLMTQUHAZnk",
            "RghnAkDBDI4", "yfsEseIcEXr",
            "lpGYJoVUudr", "ej1YwWaYGmm",
            "I7cmT3iXT0y"
        )

        facilities = FacilityFactory.getListOf(3)
        destinations = DestinationFactory.getListOf(5)
        recentActivities = emptyList()

        schedulerProvider = TrampolineSchedulerProvider()
        testSchedulerProvider = TestSchedulerProvider(TestScheduler())

        doReturn(
            Single.just(facilities)
        ).whenever(metadataManager).facilities(appConfig.program)

        `when`(metadataManager.destinations(appConfig.distributedTo))
            .thenReturn(Single.just(destinations))

        `when`(userActivityDao.getRecentActivities(Constants.USER_ACTIVITY_COUNT))
            .thenReturn(Single.just(recentActivities))

        userActivityRepository = UserActivityRepository(userActivityDao)
        userManager = UserManagerImpl(d2)
        viewModel = HomeViewModel(
            disposable,
            schedulerProvider,
            preferenceProvider,
            metadataManager,
            userActivityRepository,
            getStateHandle()
        )

        viewModel.facilities.observeForever(facilitiesObserver)
        viewModel.destinationsList.observeForever(destinationsObserver)

    }

    private fun getStateHandle(): SavedStateHandle {
        val state = hashMapOf<String, Any>(
            Constants.INTENT_EXTRA_APP_CONFIG to appConfig
        )
        return SavedStateHandle(state)
    }

    @After
    fun tearDown() {
        disposable.dispose()
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    private fun getTime() =
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun getTime(dateTime: LocalDateTime) =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()


    @Test
    fun init_shouldLoadFacilities() {
        verify(metadataManager).facilities(appConfig.program)

        assertEquals(viewModel.facilities.value, OperationState.Success(facilities))
    }

    @Test
    fun init_shouldLoadDestinations() {
        verify(metadataManager).destinations(appConfig.distributedTo)

        viewModel.destinationsList.observeForever {
            assertEquals(it, OperationState.Success(destinations))
        }
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
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_transaction_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_distributed_to_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setTransactionDate(
            LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        )
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destinations[0])
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityAndTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(getTime())
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_distributed_to_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyDestinedToAndTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destinations[0])
        viewModel.setTransactionDate(getTime())
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityAndDestinedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun distributionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        viewModel.setTransactionDate(getTime())
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facilities[0])
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun discardTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun discardTransaction_throwsErrorIfDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setDestination(destinations[1])
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facilities[0])
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun correctionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(getTime())

        assertEquals(viewModel.checkForFieldErrors(), null)
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

    @Test
    fun distributionWithMissingTransactionDate_willCreateUSerIntentWithCurrentDay() {
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[1])

        val data = viewModel.getData()
        assertEquals(data.transactionDate, now.humanReadableDate())
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
        assertEquals(
            data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility)
        )
        assertEquals(
            data.distributedTo,
            ParcelUtils.distributedTo_ToIdentifiableModelParcel(destination)
        )
        println(data)
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    @Test
    fun discardWithCompleteInformation_canCreateUserIntent() {
        val facility = facilities[1]
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facility)
        viewModel.setTransactionDate(getTime(now))
        println("A ${getTime(now)}")
        val data = viewModel.getData()
        assertEquals(data.transactionType, TransactionType.DISCARD)
        assertEquals(
            data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility)
        )
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
        assertEquals(
            data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility)
        )
        assertEquals(data.transactionDate, now.humanReadableDate())
    }
}