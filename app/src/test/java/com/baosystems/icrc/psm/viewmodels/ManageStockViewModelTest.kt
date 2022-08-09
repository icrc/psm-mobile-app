package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagedList
import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.DestinationFactory
import com.baosystems.icrc.psm.data.FacilityFactory
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.services.MetadataManager
import com.baosystems.icrc.psm.services.SpeechRecognitionManager
import com.baosystems.icrc.psm.services.StockManager
import com.baosystems.icrc.psm.services.preferences.PreferenceProvider
import com.baosystems.icrc.psm.services.rules.RuleValidationHelper
import com.baosystems.icrc.psm.services.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.services.scheduler.TrampolineSchedulerProvider
import com.baosystems.icrc.psm.ui.base.ItemWatcher
import com.baosystems.icrc.psm.ui.managestock.ManageStockViewModel
import com.baosystems.icrc.psm.utils.ParcelUtils
import com.github.javafaker.Faker
import io.reactivex.disposables.CompositeDisposable
import org.hisp.dhis.android.core.attribute.AttributeValue
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import timber.log.Timber

@RunWith(MockitoJUnitRunner::class)
class ManageStockViewModelTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var facility: IdentifiableModel
    private lateinit var distributedTo: IdentifiableModel
    private lateinit var transactionDate: String

    private val disposable = CompositeDisposable()
    private val faker = Faker()

    private lateinit var schedulerProvider: BaseSchedulerProvider
    private lateinit var appConfig: AppConfig

    @Mock
    private lateinit var ruleValidationHelperImpl: RuleValidationHelper
    @Mock
    private lateinit var speechRecognitionManagerImpl: SpeechRecognitionManager

    @Mock
    private lateinit var metadataManager: MetadataManager
    @Mock
    private lateinit var preferenceProvider: PreferenceProvider
    @Mock
    private lateinit var stockManager: StockManager
    @Mock
    private lateinit var stockItemsObserver: Observer<PagedList<AttributeValue>>
    @Captor
    private lateinit var stockItemsCaptor: ArgumentCaptor<PagedList<AttributeValue>>

    private fun getModel() =
        ManageStockViewModel(
            SavedStateHandle(),
            disposable,
            appConfig,
            schedulerProvider,
            preferenceProvider,
            stockManager,
            ruleValidationHelperImpl,
            speechRecognitionManagerImpl
        )

    private fun createStockEntry(
        uid: String,
        viewModel: ManageStockViewModel,
        qty: String?
    ): StockItem {
        val stockItem = StockItem(
            uid, faker.name().name(), faker.number().numberBetween(1, 800).toString())

        viewModel.addItem(stockItem, qty, stockItem.stockOnHand, false)

        return stockItem
    }

    @Before
    fun setUp() {
        appConfig = AppConfig(
            "F5ijs28K4s8",
            "wBr4wccNBj1",
            "sLMTQUHAZnk",
            "RghnAkDBDI4",
            "yfsEseIcEXr",
            "lpGYJoVUudr",
            "ej1YwWaYGmm",
            "I7cmT3iXT0y"
        )

        facility = ParcelUtils.facilityToIdentifiableModelParcel(
            FacilityFactory.create(57L))
        distributedTo = ParcelUtils.distributedTo_ToIdentifiableModelParcel(
            DestinationFactory.create(23L))
        transactionDate = "2021-08-05"

        schedulerProvider = TrampolineSchedulerProvider()
    }

    @Test
    fun init_shouldSetFacilityDateAndDistributedToForDistribution() {
        val viewModel = getModel()

        viewModel.transaction?.let {
            assertNotNull(it.facility)
            assertEquals(it.facility.displayName, facility.displayName)
            assertEquals(it.distributedTo!!.displayName, distributedTo.displayName)
            assertEquals(it.transactionDate, transactionDate)
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun init_distributedToMustBeSetForDistribution() {
        getModel()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun init_distributedToMustNotBeSetForDiscard() {
        getModel()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun init_distributedToMustNotBeSetForCorrection() {
        getModel()
    }

    @Test
    fun init_shouldSetFacilityAndDateForDiscard() {
        val viewModel = getModel()

        viewModel.transaction?.let {
            assertNotNull(it.facility)
            assertNull(it.distributedTo)
            assertEquals(it.facility.displayName, facility.displayName)
            assertEquals(it.transactionDate, transactionDate)
        }
    }

    @Test
    fun init_shouldSetFacilityAndDateForCorrection() {
        val viewModel = getModel()

        viewModel.transaction?.let {

            assertNotNull(it.facility)
            assertNull(it.distributedTo)
            assertEquals(it.facility.displayName, facility.displayName)
            assertEquals(it.transactionDate, transactionDate)
        }

    }

//    @Test
//    fun init_shouldHaveNoQuantitiesAlreadySet() {
//
//    }

    @Test
    fun canSetAndGetItemQuantityForSelectedItem() {
        val viewModel = getModel()

        val qty = 319L
        val item = createStockEntry("someUid", viewModel, qty.toString())

        viewModel.setQuantity(item, 0, qty.toString(),
            object : ItemWatcher.OnQuantityValidated {
                override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                    Timber.tag("ruleEffects2").d("$ruleEffects")
                }

            })
        println(item)
        println(viewModel.getItemQuantity(item))
        assertEquals(viewModel.getItemQuantity(item)?.toLong(), qty)
    }

    @Test
    fun canUpdateExistingItemQuantityForSelectedItem() {
        val viewModel = getModel()
        val qty2 = 95L
        val item = createStockEntry("someUid", viewModel, qty2.toString())
        val qty = 49

        viewModel.setQuantity(item, 0, qty.toString(),
            object : ItemWatcher.OnQuantityValidated {
                override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                    println("$ruleEffects")
                }
            })

        assertEquals(viewModel.getItemQuantity(item), qty2.toString())
    }

    // TODO: init_shouldFetchAllStockItems
//    @Test
//    fun init_shouldFetchAllStockItems() {
//        val viewModel = getModel()
//        viewModel.getStockItems().observeForever(stockItemsObserver)
//
//        val search = ""
//        viewModel.setSearchTerm(search)
//        verify(metadataManager).queryStock(search)
//        verify(stockItemsObserver).onChanged(stockItemsCaptor.capture())
//    }

    // DESCOMENTAR ESTES DOIS MËTODO
//    // TODO: Implement shouldSearchStockItems_onSearchQueryChange()
//    @Test
//    fun shouldSearchStockItems_onSearchQueryChange() {
//
//    }
//
//    @Test
//    fun canFetchPaginatedStockItems() {
//
//    }

//    @Test
//    fun canSetQueryStockList() {
//        val viewModel = getModel()
//        val q = "Parac"
//        viewModel.setSearchTerm(q)
//
//        assertEquals(viewModel.search.value, q)
//    }
}