package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.baosystems.icrc.psm.data.DestinationFactory
import com.baosystems.icrc.psm.data.FacilityFactory
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.exceptions.UserIntentParcelCreationException
import com.baosystems.icrc.psm.service.*
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.service.scheduler.TrampolineSchedulerProvider
import com.baosystems.icrc.psm.utils.ParcelUtils
import com.baosystems.icrc.psm.utils.humanReadableDate
import com.baosystems.icrc.psm.viewmodels.home.HomeViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
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
import org.mockito.kotlin.*
import java.lang.UnsupportedOperationException
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class HomeViewModelUnitTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var metadataManager: MetadataManager
    private lateinit var viewModel: HomeViewModel
    private lateinit var userManager: UserManager
    private lateinit var schedulerProvider: BaseSchedulerProvider
    private lateinit var facilities: List<OrganisationUnit>
    private lateinit var destinations: List<Option>
    private val disposable = CompositeDisposable()

    @Mock
    private lateinit var d2: D2

    @Mock
    private lateinit var facilitiesObserver: Observer<List<OrganisationUnit>>

    @Mock
    private lateinit var destinationsObserver: Observer<List<Option>>

    @Captor
    private lateinit var facilitiesArgumentCaptor: ArgumentCaptor<List<OrganisationUnit>>

    @Captor
    private lateinit var destinationsArgumentCaptor: ArgumentCaptor<List<Option>>

    @Before
    fun setup() {
        facilities = FacilityFactory.getListOf(3)
        destinations = DestinationFactory.getListOf(5)

        schedulerProvider = TrampolineSchedulerProvider()

        doReturn(
            Single.just(facilities)
        ).whenever(metadataManager).facilities()

        Mockito.`when`(metadataManager.destinations())
            .thenReturn(Single.just(destinations))

        userManager = UserManagerImpl(d2)
        viewModel = HomeViewModel(disposable, schedulerProvider, metadataManager, userManager)

        viewModel.facilities.observeForever(facilitiesObserver)
        viewModel.destinationsList.observeForever(destinationsObserver)
    }

    @Test
    fun init_shouldLoadProgram() {
        verify(metadataManager).stockManagementProgram()
        assertNotNull(viewModel.program)
    }

    @Test
    fun init_shouldLoadFacilities() {
        verify(metadataManager).facilities()
        verify(facilitiesObserver, times(1))
            .onChanged(facilitiesArgumentCaptor.capture())

        assertEquals(viewModel.facilities.value, facilities)
        assertEquals(facilitiesArgumentCaptor.value, facilities)
    }

    @Test
    fun init_shouldLoadDestinations() {
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
        viewModel.transactionDate.value = null
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
        viewModel.setTransactionDate(LocalDateTime.now())

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
        viewModel.setTransactionDate(LocalDateTime.now())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyDestinedToAndTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destinations[0])
        viewModel.setTransactionDate(LocalDateTime.now())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityAndDestinedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        removeDefaultTransactionDate()

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun distributionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        viewModel.setTransactionDate(LocalDateTime.now())

        assertEquals(viewModel.readyManageStock(), true)
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

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setTransactionDate(LocalDateTime.now())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun discardTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(LocalDateTime.now())

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
        viewModel.setTransactionDate(LocalDateTime.now())

        assertEquals(viewModel.readyManageStock(), false)
    }

    @Test
    fun correctionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facilities[0])
        viewModel.setTransactionDate(LocalDateTime.now())

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
        viewModel.setTransactionDate(LocalDateTime.now())

        viewModel.getData()
    }

    @Test(expected = UserIntentParcelCreationException::class)
    fun distributionWithMissingFacility_cannotCreateUserIntent() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setTransactionDate(LocalDateTime.now())

        viewModel.getData()
    }

    @Test(expected = UserIntentParcelCreationException::class)
    fun distributionWithMissingTransactionDate_cannotCreateUserIntent() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[1])
        viewModel.transactionDate.value = null

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
        viewModel.setTransactionDate(now)

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
        viewModel.setTransactionDate(now)

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
        viewModel.setTransactionDate(now)

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