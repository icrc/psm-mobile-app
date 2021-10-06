package com.baosystems.icrc.psm.service

import io.reactivex.Single
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

interface MetadataManager {
    fun stockManagementProgram(): Single<Program>
    fun facilities(): Single<MutableList<OrganisationUnit>>
    fun destinations(): Single<List<Option>>
    fun stockItems(program: Program, ou: OrganisationUnit):
            Single<MutableList<TrackedEntityInstance>>
}