package com.baosystems.icrc.psm.services.rules

import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import io.reactivex.Flowable
import org.hisp.dhis.rules.models.RuleEffect
import java.util.*

interface RuleValidationHelper {
    fun evaluate(entry: StockEntry, eventDate: Date, program: String,
                 transaction: Transaction): Flowable<List<RuleEffect>>
}