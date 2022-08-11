package com.baosystems.icrc.psm.services

import com.baosystems.icrc.psm.exceptions.InitializationException
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import javax.inject.Inject

class MetadataManagerImpl @Inject constructor(
    private val d2: D2,
) : MetadataManager {

    override fun stockManagementProgram(programUid: String): Single<Program?> {
        return Single.just(programUid).map {
            if (it.isBlank())
                throw InitializationException(
                    "The program config has not been set in the configuration file"
                )

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
                // TODO: Flag situations where the intersection is nil (i.e. no facility obtained)
                d2.organisationUnitModule()
                    .organisationUnits()
                    .byOrganisationUnitScope(
                        OrganisationUnit.Scope.SCOPE_DATA_CAPTURE
                    )
                    .byProgramUids(listOf(program.uid()))
                    .blockingGet()
            }
        }
    }

    override fun destinations(distributedTo: String): Single<List<Option>> {
        return Single.defer {
            d2.dataElementModule()
                .dataElements()
                .uid(distributedTo)
                .get()
                .flatMap {
                    d2.optionModule()
                        .options()
                        .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                        .byOptionSetUid()
                        .eq(it.optionSetUid())
                        .get()
                }
        }
    }
}