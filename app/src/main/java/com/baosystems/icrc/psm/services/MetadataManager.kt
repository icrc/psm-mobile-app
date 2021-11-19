package com.baosystems.icrc.psm.services

import io.reactivex.Single
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program

interface MetadataManager {
    fun stockManagementProgram(): Single<Program?>
    fun facilities(): Single<MutableList<OrganisationUnit>>
    fun destinations(): Single<List<Option>>
}