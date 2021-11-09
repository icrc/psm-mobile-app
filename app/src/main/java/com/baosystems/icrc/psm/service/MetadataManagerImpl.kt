package com.baosystems.icrc.psm.service

import com.baosystems.icrc.psm.data.models.AppConfig
import com.baosystems.icrc.psm.exceptions.InitializationException
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import timber.log.Timber

class MetadataManagerImpl(
    private val d2: D2,
    private val config: AppConfig
): MetadataManager {
    init {
//        Log.d(TAG, "Downloading metadata...")

        // TODO: Remove later. Add to a dedicated Activity (currently being used for testing)
        // TODO: Metadata error can occur, ensure you handle such situations
//        d2.metadataModule()
//            .download()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnComplete {
//                Log.i(TAG, "Finished downloading metadata!")
//            }
////            .doOnError(Throwable::getStackTrace)
//            .doOnError{
//                Log.e(TAG, "Error downloading metadata: ${it.localizedMessage}")
//                it.printStackTrace()
//            }
//            .subscribe()
//
        // TODO: Remove later, temporarily used to test functionality
//        Timber.i("Downloading TEI data...")
//        d2.trackedEntityModule()
//            .trackedEntityInstanceDownloader()
//            .byProgramUid("F5ijs28K4s8")
//            .limitByOrgunit(true)
//            .limitByProgram(true)
//            .download()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnComplete {
//                Timber.i("Finished downloading TEI data!")
//            }
//            .doOnError{
//                Timber.e("Error downloading TEI data: ${it.localizedMessage}")
//                it.printStackTrace()
//            }
//            .subscribe()
    }

    override fun stockManagementProgram(): Single<Program?> {
        return Single.just(config.program).map { programUid ->
            if (programUid.isBlank())
                throw InitializationException(
                    "The program config has not been set in the configuration file")

            d2.programModule()
                .programs()
                .byUid()
                .eq(programUid)
                .one()
                .blockingGet()
        }
    }

    /**
     * Get the program OUs which the user has access to and also
     * set as the user's the data capture OU. This is simply the
     * intersection of the program OUs (without DESCENDANTS) and
     * the user data capture OUs (with DESCENDANTS)
     */
    override fun facilities(): Single<MutableList<OrganisationUnit>> {
        return Single.defer {
            stockManagementProgram().map { program ->
                Timber.d("Base program = ${program.uid()}")
                d2.organisationUnitModule()
                    .organisationUnits()
                    .byOrganisationUnitScope(
                        OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .byProgramUids(listOf(program.uid()))
                    .blockingGet()
            }

//            d2.programModule()
//                .programs()
//                .get()
//                .map{ program ->
//                    val programIds = program.map { prg -> prg.uid() }
//                    Log.d(TAG, "Program id: $programIds")
//
//                    // TODO Flag situations where the intersection is nil
//
//                    d2.organisationUnitModule()
//                        .organisationUnits()
//                        .byOrganisationUnitScope(
//                            OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
//                        .byProgramUids(programIds)
//                        .blockingGet()
//                }
        }
    }

    override fun destinations(): Single<List<Option>> {
        return Single.defer {
            Timber.d("Fetching optionsets...")
            d2.programModule()
                .programStageDataElements()
                    // TODO: Cleanup the implementation below with proper variable names and follow through
                .get().map { psdes ->
                    println("PSDEs: ${psdes.size}")
                    println("==================================")
                    psdes.forEach {
                        println(it.uid())
                    }

                    val dataElements = psdes.map { psde -> psde.dataElement() }
                    println("Data Elements: ${dataElements.size}")
                    println("==================================")

                    val nonEmptyOptionSets = dataElements.filterNotNull()
                        .map {de ->
                            println("${de.uid()} - ${de.name()} - ${de.optionSet()}")

                            d2.dataElementModule().dataElements()
                                .byUid().eq(de.uid()).one()
                                .blockingGet()
                    }
                        .filter { de -> de?.optionSet() != null }

                    println("nonEmptyOptionSets: ${nonEmptyOptionSets.size}")
                    println("==================================")

                    nonEmptyOptionSets.forEach {
                        println("${it?.uid()} - ${it?.name()}")
                    }

                    val optionSetUids = nonEmptyOptionSets.map { de -> de.optionSetUid() }
                    Timber.d("Optionset uids: $optionSetUids")

                    // TODO: Removing the flatten() call if you wouldn't be looping
                    //  through all the DEs. Final decision will be taken when you hear from David
                    nonEmptyOptionSets.map { de -> de.optionSetUid() }.map { uid ->
                        d2.optionModule()
                            .options()
                            .byOptionSetUid()
                            .eq(uid)
                            .orderByDisplayName(RepositoryScope.OrderByDirection.DESC)
                            .blockingGet()
                    }.flatten()
                }
        }
    }
}