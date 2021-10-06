package com.baosystems.icrc.psm.service

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class MetadataManagerImpl(private val d2: D2): MetadataManager {
    val TAG = "MetadataManagerImpl"

    init {
        Log.d(TAG, "Downloading metadata...")

        // TODO: Remove later. Add to a dedicated Activity (currently being used for testing)
        // TODO: Metadata error can occur, ensure you handle such situations
        d2.metadataModule()
            .download()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                Log.i(TAG, "Finished downloading metadata!")
            }
//            .doOnError(Throwable::getStackTrace)
            .doOnError{
                Log.e(TAG, "Error downloading metadata: ${it.localizedMessage}")
                it.printStackTrace()
            }
            .subscribe()
    }

    override fun stockManagementProgram(): Single<Program> {
        return d2.programModule().programs()
            .byName()
                // TODO: Inject program name from config
            .like("Pharmacy Stock Management")
            .one()
            .get()
    }

    /**
     * Get the program OUs which the user has access to and also
     * set as the user's the data capture OU. This is simply the
     * intersection of the program OUs (without DESCENDANTS) and
     * the user data capture OUs (with DESCENDANTS)
     */
    override fun facilities(): Single<MutableList<OrganisationUnit>> {
        Log.d("TAG", "Looking up facilities (program OUs)...")

        // TODO: If the list of programs returned is more than one, flag it
        return Single.defer {
            d2.programModule()
                .programs()
                .get()
                .map{ program ->
                    val programIds = program.map { prg -> prg.uid() }
                    Log.d(TAG, "Program id: $programIds")

                    // TODO Flag situations where the intersection is nil

                    d2.organisationUnitModule()
                        .organisationUnits()
                        .byOrganisationUnitScope(
                            OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .byProgramUids(programIds)
                        .blockingGet()
                }
        }
    }

    override fun destinations(): Single<List<Option>> {
        return Single.defer {
            Log.d(TAG, "Fetching optionsets...")
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
                    Log.d(TAG, "Optionset uids: ${optionSetUids}")

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

    override fun stockItems(program: Program, ou: OrganisationUnit):
            Single<MutableList<TrackedEntityInstance>> {

        return d2.trackedEntityModule().trackedEntityInstances()
            .byProgramUids(
                listOf(program.uid())
            )
            .byOrganisationUnitUid()
            .eq(ou.uid())
            .get()
    }
}