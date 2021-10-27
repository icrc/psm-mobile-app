package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.DestinationFactory
import com.baosystems.icrc.psm.data.FacilityFactory
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.service.MetadataManager
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.service.scheduler.TrampolineSchedulerProvider
import com.baosystems.icrc.psm.utils.ParcelUtils
import com.baosystems.icrc.psm.viewmodels.stock.ManageStockViewModel
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.attribute.AttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ManageStockViewModelTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var facility: IdentifiableModel
    private lateinit var distributedTo: IdentifiableModel
    private lateinit var transactionDate: String

    private val disposable = CompositeDisposable()
    private lateinit var schedulerProvider: BaseSchedulerProvider

    @Mock
    private lateinit var metadataManager: MetadataManager
    @Mock
    private lateinit var stockItemsObserver: Observer<PagedList<AttributeValue>>
    @Captor
    private lateinit var stockItemsCaptor: ArgumentCaptor<PagedList<AttributeValue>>

    private fun getModel(type: TransactionType,
                         distributedTo: IdentifiableModel?) =
        ManageStockViewModel(
            disposable,
            schedulerProvider,
            metadataManager,
            type,
            facility,
            transactionDate,
            distributedTo
        )

    private fun createTEI(uid: String) = TrackedEntityInstance.builder().uid(uid).build()

    @Before
    fun setUp() {
        facility = ParcelUtils.facilityToIdentifiableModelParcel(
            FacilityFactory.create(57L))
        distributedTo = ParcelUtils.distributedTo_ToIdentifiableModelParcel(
            DestinationFactory.create(23L))
        transactionDate = "2021-08-05"

        schedulerProvider = TrampolineSchedulerProvider()
    }

    @Test
    fun init_shouldSetFacilityDateAndDistributedToForDistribution() {
        val viewModel = getModel(TransactionType.DISTRIBUTION, distributedTo)

        assertNotNull(viewModel.facility)
        assertEquals(viewModel.facility.displayName, facility.displayName)
        assertEquals(viewModel.distributedTo!!.displayName, distributedTo.displayName)
        assertEquals(viewModel.transactionDate, transactionDate)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun init_distributedToMustBeSetForDistribution() {
        getModel(TransactionType.DISTRIBUTION, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun init_distributedToMustNotBeSetForDiscard() {
        getModel(TransactionType.DISCARD, distributedTo)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun init_distributedToMustNotBeSetForCorrection() {
        getModel(TransactionType.CORRECTION, distributedTo)
    }

    @Test
    fun init_shouldSetFacilityAndDateForDiscard() {
        val viewModel = getModel(TransactionType.DISCARD, null)

        assertNotNull(viewModel.facility)
        assertNull(viewModel.distributedTo)
        assertEquals(viewModel.facility.displayName, facility.displayName)
        assertEquals(viewModel.transactionDate, transactionDate)
    }

    @Test
    fun init_shouldSetFacilityAndDateForCorrection() {
        val viewModel = getModel(TransactionType.CORRECTION, null)

        assertNotNull(viewModel.facility)
        assertNull(viewModel.distributedTo)
        assertEquals(viewModel.facility.displayName, facility.displayName)
        assertEquals(viewModel.transactionDate, transactionDate)
    }

//    @Test
//    fun init_shouldHaveNoQuantitiesAlreadySet() {
//
//    }

    @Test
    fun canSetAndGetItemQuantityForSelectedItem() {
        val viewModel = getModel(TransactionType.DISTRIBUTION, distributedTo)

        val item = createTEI("someUid")
        val qty = 319

        viewModel.setItemQuantity(item, qty)
        assertEquals(viewModel.getItemQuantity(item), qty)
    }

    @Test
    fun canUpdateExistingItemQuantityForSelectedItem() {
        val viewModel = getModel(TransactionType.DISTRIBUTION, distributedTo)
        val qty2 = 95
        val item = createTEI("someUid")

        viewModel.setItemQuantity(item, 49)
        viewModel.setItemQuantity(item, qty2)
        assertEquals(viewModel.getItemQuantity(item), qty2)
    }

    // TODO: init_shouldFetchAllStockItems
//    @Test
//    fun init_shouldFetchAllStockItems() {
//        val viewModel = getModel(TransactionType.DISTRIBUTION, distributedTo)
//        viewModel.getStockItems().observeForever(stockItemsObserver)
//
//        val search = ""
//        viewModel.setSearchTerm(search)
//        verify(metadataManager).queryStock(search)
//        verify(stockItemsObserver).onChanged(stockItemsCaptor.capture())
//    }

    // TODO: Implement shouldSearchStockItems_onSearchQueryChange()
    @Test
    fun shouldSearchStockItems_onSearchQueryChange() {

    }

    @Test
    fun canFetchPaginatedStockItems() {

    }

//    @Test
//    fun canSetQueryStockList() {
//        val viewModel = getModel(TransactionType.DISTRIBUTION, distributedTo)
//        val q = "Parac"
//        viewModel.setSearchTerm(q)
//
//        assertEquals(viewModel.search.value, q)
//    }
}