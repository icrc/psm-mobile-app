package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.baosystems.icrc.psm.data.DestinationFactory
import com.baosystems.icrc.psm.data.FacilityFactory
import com.baosystems.icrc.psm.service.*
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.service.scheduler.TrampolineSchedulerProvider
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.assertEquals
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

@RunWith(MockitoJUnitRunner::class)
class HomeViewModelUnitTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var metadataManager: MetadataManager
    private lateinit var viewModel: HomeViewModel
    private lateinit var userManager: UserManager
    private lateinit var schedulerProvider: BaseSchedulerProvider
    private lateinit var organisationUnits: List<OrganisationUnit>
    private lateinit var destinations: List<Option>

    @Mock
    private lateinit var d2: D2

//    private lateinit var facilitiesObserver: Single<List<OrganisationUnit>>
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
        organisationUnits = FacilityFactory.getListOf(3)
        destinations = DestinationFactory.getListOf(5)

        schedulerProvider = TrampolineSchedulerProvider()

        doReturn(
            Single.just(organisationUnits)
        ).whenever(metadataManager).facilities()

        Mockito.`when`(metadataManager.destinations())
            .thenReturn(Single.just(destinations))

        userManager = UserManagerImpl(d2)
        viewModel = HomeViewModel(schedulerProvider, metadataManager, userManager)

        viewModel.facilities.observeForever(facilitiesObserver)
        viewModel.destinationsList.observeForever(destinationsObserver)
    }

    @Test
    fun init_shouldLoadFacilities() {
        verify(metadataManager).facilities()
        verify(facilitiesObserver, times(1))
            .onChanged(facilitiesArgumentCaptor.capture())


        assertEquals(viewModel.facilities.value, organisationUnits)
        assertEquals(facilitiesArgumentCaptor.value, organisationUnits)
    }

    @Test
    fun init_shouldLoadDestinations() {
        verify(metadataManager).destinations()
        verify(destinationsObserver, times(1))
            .onChanged(destinationsArgumentCaptor.capture())

        assertEquals(destinationsArgumentCaptor.value, destinations)
    }
}