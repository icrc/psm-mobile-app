package com.baosystems.icrc.psm.services.rules

import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.utils.RuleEngineHelper
import com.baosystems.icrc.psm.utils.toRuleList
import com.baosystems.icrc.psm.utils.toRuleVariableList
import io.reactivex.Flowable
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
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
    appConfig: AppConfig,
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

    override fun evaluate(program: String, ou: String, eventDate: Date,
                          values: Map<String, String>): Flowable<List<RuleEffect>> {
        return ruleEngine().flatMap { ruleEngine ->
            val programStage = programStage(program)
            val dataValues = values.map { RuleDataValue.create(eventDate, programStage.uid(), it.key, it.value) }
            Flowable.fromCallable(
                ruleEngine.evaluate(
                    createRuleEvent(programStage, ou, dataValues, Date())
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
}



