package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.baosystems.icrc.psm.data.DestinationFactory
import com.baosystems.icrc.psm.data.FacilityFactory
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.IdentifiableModel
import com.baosystems.icrc.psm.utils.ParcelUtils
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.lang.UnsupportedOperationException

@RunWith(MockitoJUnitRunner::class)
class ManageStockViewModelTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var facility: IdentifiableModel
    private lateinit var distributedTo: IdentifiableModel
    lateinit var transactionDate: String

    private fun getModel(type: TransactionType) =
        ManageStockViewModel(
            type, facility,
            transactionDate,
            if (type == TransactionType.DISTRIBUTION) distributedTo else null
        )

    private fun getModel(type: TransactionType,
                         distributedTo: IdentifiableModel?) =
        ManageStockViewModel(
            type, facility,
            transactionDate,
            distributedTo
        )

    @Before
    fun setUp() {
        facility = ParcelUtils.facilityToIdentifiableModelParcel(
            FacilityFactory.getListOf(1).first()
        )
        distributedTo = ParcelUtils.distributedTo_ToIdentifiableModelParcel(
            DestinationFactory.create(23L))
        transactionDate = "2021-08-05"
    }

    @Test
    fun init_shouldSetFacilityDateAndDistributedToForDistribution() {
        val viewModel = getModel(TransactionType.DISTRIBUTION)

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
        val viewModel = getModel(TransactionType.DISCARD)

        assertNotNull(viewModel.facility)
        assertNull(viewModel.distributedTo)
        assertEquals(viewModel.facility.displayName, facility.displayName)
        assertEquals(viewModel.transactionDate, transactionDate)
    }

    @Test
    fun init_shouldSetFacilityAndDateForCorrection() {
        val viewModel = getModel(TransactionType.CORRECTION)

        assertNotNull(viewModel.facility)
        assertNull(viewModel.distributedTo)
        assertEquals(viewModel.facility.displayName, facility.displayName)
        assertEquals(viewModel.transactionDate, transactionDate)
    }
}