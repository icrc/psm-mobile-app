package com.baosystems.icrc.psm.services.rules

import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.data.models.Transaction
import io.reactivex.Flowable
import org.hisp.dhis.rules.models.RuleEffect
import java.util.*

interface RuleValidationHelper {
    fun evaluate(item: StockItem, qty: Long?, eventDate: Date, program: String,
                 transaction: Transaction): Flowable<List<RuleEffect>>
}