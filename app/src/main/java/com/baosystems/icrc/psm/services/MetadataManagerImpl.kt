package com.baosystems.icrc.psm.services

import com.baosystems.icrc.psm.exceptions.InitializationException
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import timber.log.Timber
import javax.inject.Inject

class MetadataManagerImpl @Inject constructor(val d2: D2): MetadataManager {
    override fun stockManagementProgram(programUid: String): Single<Program?> {
        return Single.just(programUid).map { programUid ->
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
    override fun facilities(programUid: String): Single<MutableList<OrganisationUnit>> {
        return Single.defer {
            stockManagementProgram(programUid).map { program ->
                Timber.d("Base program = ${program.uid()}")
                // TODO Flag situations where the intersection is nil (i.e. no facility obtained)
                d2.organisationUnitModule()
                    .organisationUnits()
                    .byOrganisationUnitScope(
                        OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .byProgramUids(listOf(program.uid()))
                    .blockingGet()
            }
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