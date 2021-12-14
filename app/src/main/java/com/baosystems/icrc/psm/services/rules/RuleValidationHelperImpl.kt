package com.baosystems.icrc.psm.services.rules

import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.StockItem
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.utils.*
import io.reactivex.Flowable
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleVariable
import java.util.*
import javax.inject.Inject

class RuleValidationHelperImpl @Inject constructor(
    private val d2: D2,
    private val appConfig: AppConfig,
): RuleValidationHelper {

    private var cachedRuleEngine: Flowable<RuleEngine>
    private var ruleEngine: RuleEngine? = null

    init {
        cachedRuleEngine = Single.zip(
            programRules(appConfig.program),
                ruleVariables(appConfig.program),
                constants(),
                supplementaryData(), {
                    rules, variables, constants, supplData ->
                        RuleEngineHelper.getRuleEngine(rules, variables, constants, supplData)
                })
            .doOnSuccess { ruleEngine = it }
            .toFlowable()
            .cacheWithInitialCapacity(1)
    }

    override fun evaluate(
        item: StockItem,
        qty: Long?,
        eventDate: Date,
        program: String,
        transaction: Transaction
    ): Flowable<List<RuleEffect>> {
        return ruleEngine().flatMap { ruleEngine ->
            val programStage = programStage(program)
            val dataValues = dataValues(item.id, qty, programStage.uid(), transaction, eventDate)
            Flowable.fromCallable(
                ruleEngine.evaluate(
                    createRuleEvent(programStage, transaction.facility.uid, dataValues, eventDate)
                )
            )
        }
    }

    private fun createRuleEvent(
        programStage: ProgramStage,
        organisationUnit: String,
        dataValues: List<RuleDataValue>,
        period: Date
    ) = RuleEvent.create(
        UUID.randomUUID().toString(), programStage.uid(), RuleEvent.Status.ACTIVE, period, period,
        organisationUnit, null, dataValues,
        programStage.name() ?: "", period
    )

    private fun organisationUnit(uid: String) =
        d2.organisationUnitModule().organisationUnits().byUid().eq(uid).one().blockingGet()

    private fun programStage(programUid: String) =
        d2.programModule().programStages()
            .byProgramUid().eq(programUid)
            .one().blockingGet()

    private fun ruleVariables(programUid: String): Single<List<RuleVariable>> {
        return d2.programModule().programRuleVariables()
            .byProgramUid().eq(programUid)
            .get()
            .map {
                it.toRuleVariableList(
                    d2.trackedEntityModule().trackedEntityAttributes(),
                    d2.dataElementModule().dataElements()
                )
            }
    }

    private fun constants(): Single<Map<String, String>> {
        return d2.constantModule().constants().get()
            .map { constants ->
                val constantsMap = HashMap<String, String>()
                for (constant in constants) {
                    constantsMap[constant.uid()] =
                        Objects.requireNonNull<Double>(constant.value()).toString()
                }
                constantsMap
            }
    }

    private fun supplementaryData(): Single<Map<String, List<String>>> = Single.just(hashMapOf())

    private fun ruleEngine(): Flowable<RuleEngine> =
        if (ruleEngine != null) { Flowable.just(ruleEngine) } else { cachedRuleEngine }

    private fun programRules(programUid: String, eventUid: String? = null) =
        d2.programModule().programRules()
            .byProgramUid().eq(programUid)
            .withProgramRuleActions()
            .get()
            .map { it.toRuleList() }
            .map {
                if (eventUid != null) {
                    val programStage = d2.eventModule().events()
                        .uid(eventUid).blockingGet().programStage()
                    it.filter { rule ->
                        rule.programStage() == null || rule.programStage() == programStage
                    }
                } else {
                    it
                }
            }

    private fun mostRecentEvent(teiUid: String): Single<Event> {
        return d2.eventModule()
            .events()
            .byTrackedEntityInstanceUids(Collections.singletonList(teiUid))
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .orderByLastUpdated(RepositoryScope.OrderByDirection.DESC)
            .one()
            .get()
    }

    private fun dataValues(
        teiUid: String,
        qty: Long?,
        programStage: String,
        transaction: Transaction,
        eventDate: Date
    ): List<RuleDataValue> {
        val values = mostRecentEvent(teiUid).map { event ->
            if (event.trackedEntityDataValues() != null) {
                event.trackedEntityDataValues()!!.toRuleDataValue(
                    event,
                    d2.dataElementModule().dataElements(),
                    d2.programModule().programRuleVariables(),
                    d2.optionModule().options()
                )
            } else {
                listOf()
            }
        }.blockingGet().toMutableList()

        // Add the quantity if defined
        if (qty != null) {
            val deUid = ConfigUtils.getTransactionDataElement(transaction.transactionType, appConfig)
            values.add(RuleDataValue.create(eventDate, programStage, deUid, qty.toString()))

            // Add the 'deliver to' if it's a distribution event
            if (transaction.transactionType == TransactionType.DISTRIBUTION) {
                transaction.distributedTo?.let { distributedTo ->
                    d2.optionModule()
                        .options()
                        .uid(distributedTo.uid)
                        .blockingGet()
                        .code()?.let { code ->
                            values.add(
                                RuleDataValue.create(eventDate, programStage, appConfig.distributedTo, code))
                        }
                }
            }
        }

        return values.toList()
    }
}



