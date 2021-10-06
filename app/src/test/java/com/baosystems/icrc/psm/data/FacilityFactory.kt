package com.baosystems.icrc.psm.data

import com.github.javafaker.Faker
import org.hisp.dhis.android.core.arch.helpers.UidGenerator
import org.hisp.dhis.android.core.arch.helpers.UidGeneratorImpl
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

object FacilityFactory {
    private val uidGenerator: UidGenerator = UidGeneratorImpl()
    private val faker: Faker = Faker()

    fun getListOf(num: Int): List<OrganisationUnit> {
        return (1..num).map {
            val name = faker.address().streetName()
            OrganisationUnit.builder()
                .id(it.toLong())
                .uid(uidGenerator.generate())
                .name(name)
                .displayName(name).build()
        }
    }
}