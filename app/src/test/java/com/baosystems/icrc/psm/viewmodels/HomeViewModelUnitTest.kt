package com.baosystems.icrc.psm.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.baosystems.icrc.psm.data.OrganisationUnitSamples
import com.baosystems.icrc.psm.service.*
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider
import com.baosystems.icrc.psm.service.scheduler.TrampolineSchedulerProvider
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
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

    @Mock
    private lateinit var d2: D2

//    private lateinit var facilitiesObserver: Single<List<OrganisationUnit>>
    @Mock
    private lateinit var facilitiesObserver: Observer<List<OrganisationUnit>>
    @Captor
    private lateinit var facilitiesArgumentCaptor: ArgumentCaptor<List<OrganisationUnit>>
    private lateinit var organisationUnits: List<OrganisationUnit?>

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        organisationUnits = listOf(OrganisationUnitSamples.getOrganisationUnit())
        schedulerProvider = TrampolineSchedulerProvider()

        doReturn(
            Single.just(organisationUnits)
        ).whenever(metadataManager).facilities()

        userManager = UserManagerImpl(d2)
        viewModel = HomeViewModel(schedulerProvider, metadataManager, userManager)

        viewModel.facilities.observeForever(facilitiesObserver)
    }

    @Test
    fun init_shouldLoadFacilities() {
        verify(metadataManager).facilities()
//        println(viewModel.facilitiesList.value)
        verify(facilitiesObserver, times(1))
            .onChanged(facilitiesArgumentCaptor.capture())
    }
}