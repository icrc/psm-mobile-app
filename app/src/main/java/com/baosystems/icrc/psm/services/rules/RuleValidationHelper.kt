package com.baosystems.icrc.psm.services.rules

import io.reactivex.Flowable
import org.hisp.dhis.rules.models.RuleEffect
import java.util.*

interface RuleValidationHelper {
    fun evaluate(program: String, ou: String, eventDate: Date,
                 values: Map<String, String>): Flowable<List<RuleEffect>>
}