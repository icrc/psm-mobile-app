package com.baosystems.icrc.psm.data

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OrganisationUnitSamples {
    companion object {
        @JvmStatic
        fun getOrganisationUnit(
            id: Long,
            uid: String
        ): OrganisationUnit? {
            return OrganisationUnit.builder()
                .id(id)
                .uid(uid)
                .build()
        }

        @JvmStatic
        fun getOrganisationUnit(
            id: Long,
            uid: String,
            name: String,
            displayName: String
        ): OrganisationUnit? {
            return OrganisationUnit.builder()
                .id(id)
                .uid(uid)
                .name(name)
                .displayName(displayName)
                .build()
        }

        @JvmStatic
        fun getOrganisationUnit(): OrganisationUnit? {
            return OrganisationUnit.builder()
                .id(1L)
                .uid("UID")
                .build()
        }

        @JvmStatic
        fun getOrganisationUnit(id: Long): OrganisationUnit? {
            return OrganisationUnit.builder()
                .id(id)
                .uid("UID")
                .build()
        }
    }
}