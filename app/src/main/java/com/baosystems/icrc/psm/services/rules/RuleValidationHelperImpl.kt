package com.baosystems.icrc.psm.services.rules

import com.baosystems.icrc.psm.data.AppConfig
import com.baosystems.icrc.psm.data.TransactionType
import com.baosystems.icrc.psm.data.models.StockEntry
import com.baosystems.icrc.psm.data.models.Transaction
import com.baosystems.icrc.psm.utils.*
import io.reactivex.Flowable
import io.reactivex.Single
import org.apache.commons.lang3.math.NumberUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.models.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class RuleValidationHelperImpl @Inject constructor(
    private val d2: D2,
    private val appConfig: AppConfig,
): RuleValidationHelper {

//    private var cachedRuleEngine: Flowable<RuleEngine>
    private var ruleEngine: RuleEngine? = null

    init {
        // TODO: Temporarily commented out the cached rule engine. Find a way to cache the rules engines list if necessary
//        cachedRuleEngine = Single.zip(
//            programRules(appConfig.program),
//                ruleVariables(appConfig.program),
//                constants(),
//                supplementaryData(), {
//                    rules, variables, constants, supplData ->
//                        RuleEngineHelper.getRuleEngine(
//                            rules,
//                            variables,
//                            constants,
//                            supplData,
//                            events
//                        )
//                })
//            .doOnSuccess { ruleEngine = it }
//            .toFlowable()
//            .cacheWithInitialCapacity(1)
    }

    override fun evaluate(
        entry: StockEntry,
        eventDate: Date,
        program: String,
        transaction: Transaction,
        eventUid: String?
    ): Flowable<List<RuleEffect>> {
        return ruleEngine(entry.item.id).flatMap { ruleEngine ->
            val programStage = programStage(program)

            Flowable.fromCallable(
                prepareForDataEntry(ruleEngine, programStage, transaction, eventDate)
            ).flatMap { prelimRuleEffects ->
                val dataValues = mutableListOf<RuleDataValue>().apply {
                    addAll(
                        entryDataValues(entry.qty, programStage.uid(), transaction, eventDate)
                    )
                }

                prelimRuleEffects.forEach { ruleEffect ->
                    when(ruleEffect.ruleAction()) {
                        is RuleActionAssign -> {
                            val ruleAction = ruleEffect.ruleAction() as RuleActionAssign
                            Timber.d("*****    Initial Data Values received:\n\n data = %s, " +
                                    "rule = %s, field = %s",
                                ruleEffect.data(), ruleAction.data(), ruleAction.field())

                            ruleEffect.data()?.let { data ->
                                dataValues.add(
                                    RuleDataValue.create(eventDate, programStage.uid(), ruleAction.field(), data)
                                )
                            }
                        }
                    }
                }

                Timber.d("Data values: %s", dataValues)

                Flowable.fromCallable(
                    ruleEngine.evaluate(
                        createRuleEvent(programStage, transaction.facility.uid, dataValues, eventDate, null)
                    )
                )
            }
        }
    }

    /**
     * Evaluate the program rules on a blank new rule event in preparation for
     * data entry
     */
    private fun prepareForDataEntry(
        ruleEngine: RuleEngine,
        programStage: ProgramStage,
        transaction: Transaction,
        eventDate: Date
    ) = ruleEngine.evaluate(
        createRuleEvent(programStage, transaction.facility.uid, listOf(), eventDate)
    )

    //===========          JAN 12 impl          =======
//    override fun evaluate(
//        entry: StockEntry,
//        eventDate: Date,
//        program: String,
//        transaction: Transaction,
//        eventUid: String?
//    ): Flowable<List<RuleEffect>> {
//        return ruleEngine(entry.item.id).flatMap { ruleEngine ->
//            val programStage = programStage(program)
//            val dataValues = dataValues(entry.item.id, entry.qty, programStage.uid(), transaction, eventDate)
//            Timber.d("Data values: %s", dataValues)
//
//            val evtId = eventUid ?: (mostRecentEvent(entry.item.id).blockingGet().uid() ?: null)
//            Timber.d("Event id: %s", evtId)
//
//            Flowable.fromCallable(
//                ruleEngine.evaluate(
//                    createRuleEvent(programStage, transaction.facility.uid, dataValues, eventDate, evtId)
//                )
//            )
//        }
//    }

//    override fun evaluate(
//        entry: StockEntry,
//        eventDate: Date,
//        program: String,
//        transaction: Transaction,
//        eventUid: String?
//    ): Flowable<List<RuleEffect>> {
//        return ruleEngine().flatMap { ruleEngine ->
//            val programStage = programStage(program)
////            val dataValues = dataValues(entry.item.id, entry.qty, programStage.uid(), transaction, eventDate)
////            var dataValues = dataValues(entry.item.id, null, programStage.uid(), transaction, eventDate)
//            val mEvt = mostRecentEvent(entry.item.id).blockingGet()
//            val evtUid = mEvt.uid()
//            Timber.d("Most recent event: %s, (last updated = %s)", mEvt.uid(), mEvt.lastUpdated())
//            var dataValues = queryDataValues(mEvt.uid()).blockingFirst()
//            Timber.d("Data values for previous event: %s", dataValues)
//
//
//            var newEvent = createRuleEvent(
//                programStage,
//                transaction.facility.uid,
//                dataValues,
//                eventDate,
//                evtUid
//            )
//            val result = ruleEngine.evaluate(newEvent)
//            val effects = result.call();
//            Timber.d("+++      Pre-evaluation call: ")
//            effects.forEach {
//                println("          Effect: ${it}")
//            }
//
////            val newDataValues = dataValues(entry.item.id, entry.qty, programStage.uid(), transaction, eventDate, evtUid)
//            val deUid = ConfigUtils.getTransactionDataElement(transaction.transactionType, appConfig)
//            val newDataValues = mutableListOf<RuleDataValue>(
//                RuleDataValue.create(eventDate, programStage.uid(), deUid, entry?.qty ?: "")
//            )
//
//            effects.forEach { re ->
//                val ruleAction = re.ruleAction()
//                if (ruleAction is RuleActionAssign) {
//                    newDataValues.add(
//                        RuleDataValue.create(eventDate, programStage.uid(), ruleAction.field(), re.data() ?: "")
//                    )
//                }
//            }
//
//            Timber.d("*******        New data values")
//            newDataValues.forEach {
//                println("         ${it.dataElement()} = ${it.value()}")
//            }
//
//            Flowable.fromCallable(
//                ruleEngine.evaluate(
////                    createRuleEvent(programStage, transaction.facility.uid, dataValues, eventDate, eventUid)
//                    createRuleEvent(programStage, transaction.facility.uid, newDataValues, eventDate)
//                )
//            )
//        }
//    }

//    override fun evaluate(
//        entry: StockEntry,
//        eventDate: Date,
//        program: String,
//        transaction: Transaction,
//        eventUid: String?
//    ): Flowable<List<RuleEffect>> {
//        return ruleEngine().flatMap { ruleEngine ->
//            val programStage = programStage(program)
////            val dataValues = dataValues(entry.item.id, entry.qty, programStage.uid(), transaction, eventDate)
////            var dataValues = dataValues(entry.item.id, null, programStage.uid(), transaction, eventDate)
//            val mEvt = mostRecentEvent(entry.item.id).blockingGet()
//            val evtUid = mEvt.uid()
//            Timber.d("Most recent event: %s, (last updated = %s)", mEvt.uid(), mEvt.lastUpdated())
//            var dataValues = queryDataValues(mEvt.uid()).blockingFirst()
//            Timber.d("Data values for previous event: %s", dataValues)
//
//
//            var newEvent = createRuleEvent(
//                programStage,
//                transaction.facility.uid,
//                dataValues,
//                eventDate,
//                evtUid
//            )
//            val result = ruleEngine.evaluate(newEvent)
//            val effects = result.call();
//            Timber.d("+++      Pre-evaluation call: ")
//            effects.forEach {
//                println("          Effect: ${it}")
//            }
//
////            val newDataValues = dataValues(entry.item.id, entry.qty, programStage.uid(), transaction, eventDate, evtUid)
//            val deUid = ConfigUtils.getTransactionDataElement(transaction.transactionType, appConfig)
//            val newDataValues = mutableListOf<RuleDataValue>(
//                RuleDataValue.create(eventDate, programStage.uid(), deUid, entry?.qty ?: "")
//            )
//
//            effects.forEach { re ->
//                val ruleAction = re.ruleAction()
//                if (ruleAction is RuleActionAssign) {
//                    newDataValues.add(
//                        RuleDataValue.create(eventDate, programStage.uid(), ruleAction.field(), re.data() ?: "")
//                    )
//                }
//            }
//
//            Timber.d("*******        New data values")
//            newDataValues.forEach {
//                println("         ${it.dataElement()} = ${it.value()}")
//            }
//
//            Flowable.fromCallable(
//                ruleEngine.evaluate(
////                    createRuleEvent(programStage, transaction.facility.uid, dataValues, eventDate, eventUid)
//                    createRuleEvent(programStage, transaction.facility.uid, newDataValues, eventDate)
//                )
//            )
//        }
//    }







    private fun createRuleEvent(
        programStage: ProgramStage,
        organisationUnit: String,
        dataValues: List<RuleDataValue>,
        period: Date,
        eventUid: String? = null
    ) = RuleEvent.create(
        eventUid ?: UUID.randomUUID().toString(), programStage.uid(),
        RuleEvent.Status.ACTIVE, period, period,
        organisationUnit, null, dataValues,
        programStage.name() ?: "", period
    )


    // LATEST
//    private fun createRuleEvent(
//        programStage: ProgramStage,
//        organisationUnit: String,
//        dataValues: List<RuleDataValue>,
//        period: Date,
//        eventUid: String? = null
//    ): RuleEvent {
//        var eventBuilder = RuleEvent.builder()
//
//        if (eventUid != null) {
//            val currentEvent =
//                d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).blockingGet()
//            val currentStage =
//                d2.programModule().programStages().uid(currentEvent.programStage()).blockingGet()
//            val ou =
//                d2.organisationUnitModule().organisationUnits().uid(currentEvent.organisationUnit())
//                    .blockingGet()
//
//            return eventBuilder
//                .event(currentEvent.uid())
//                .programStage(currentEvent.programStage())
//                .programStageName(currentStage.displayName())
//                .status(RuleEvent.Status.valueOf(currentEvent.status()!!.name))
//                .eventDate(currentEvent.eventDate())
//                .dueDate(if (currentEvent.dueDate() != null) currentEvent.dueDate() else currentEvent.eventDate())
//                .organisationUnit(currentEvent.organisationUnit())
//                .organisationUnitCode(ou.code())
//                .dataValues(dataValues)
//                .build()
//        } else {
//            return RuleEvent.create(
//                UUID.randomUUID().toString(),
//                programStage.uid(),
//                RuleEvent.Status.ACTIVE,
//                period,
//                period,
//                organisationUnit,
//                null,
//                dataValues,
//                programStage.name() ?: "", period
//            )
//        }
//    }

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

//    private fun ruleEngine(): Flowable<RuleEngine> =
//        if (ruleEngine != null) { Flowable.just(ruleEngine) } else { cachedRuleEngine }

    private fun ruleEngine(teiUid: String): Flowable<RuleEngine> {
        val enrollment = currentEnrollment(teiUid, appConfig.program)

        val enrollmentEvents = if (enrollment == null) {
            Single.just(listOf())
        } else {
            enrollmentEvents(enrollment)
        }

        return Single.zip(
            programRules(appConfig.program),
            ruleVariables(appConfig.program),
            constants(),
            supplementaryData(),
            enrollmentEvents,
            {
                    rules, variables, constants, supplData, events ->
                Timber.d("Enrollment events: %s", events)
                RuleEngineHelper.getRuleEngine(rules, variables, constants, supplData, events)
            })
            .doOnSuccess { ruleEngine = it }
            .toFlowable()
            .cacheWithInitialCapacity(1)
    }

    private fun currentEnrollment(teiUid: String, programUid: String): Enrollment? {
        val enrollments = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
            .byProgram().eq(programUid)
            .orderByEnrollmentDate(RepositoryScope.OrderByDirection.DESC)
            .blockingGet()

        var mostRecentEnrollment: Enrollment? = null
        for (enrollment in enrollments) {
            if (enrollment.status() == EnrollmentStatus.ACTIVE) {
                mostRecentEnrollment = enrollment
                break
            }
        }

        if (mostRecentEnrollment == null && enrollments.isNotEmpty())
            mostRecentEnrollment = enrollments[0]

        Timber.d("Enrollment: %s", mostRecentEnrollment)

        return mostRecentEnrollment
    }

    fun enrollmentEvents(enrollment: Enrollment): Single<List<RuleEvent>>? {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollment.uid())
            .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
            .byEventDate().beforeOrEqual(Date())
            .withTrackedEntityDataValues()
            .get()
            .toFlowable().flatMapIterable { events -> events }
            .map { event ->
                RuleEvent.builder()
                    .event(event.uid())
                    .programStage(event.programStage())
                    .programStageName(
                        d2.programModule().programStages().uid(event.programStage())
                            .blockingGet()!!.name()
                    )
                    .status(
                        if (event.status() == EventStatus.VISITED) {
                            RuleEvent.Status.ACTIVE
                        } else {
                            RuleEvent.Status.valueOf(event.status()!!.name)
                        }
                    )
                    .eventDate(event.eventDate())
                    .dueDate(if (event.dueDate() != null) event.dueDate() else event.eventDate())
                    .organisationUnit(event.organisationUnit())
                    .organisationUnitCode(
                        d2.organisationUnitModule()
                            .organisationUnits().uid(event.organisationUnit())
                            .blockingGet()!!.code()
                    )
                    .dataValues(
                        event.trackedEntityDataValues()?.toRuleDataValue(
                            event,
                            d2.dataElementModule().dataElements(),
                            d2.programModule().programRuleVariables(),
                            d2.optionModule().options()
                        )
                    )
                    .build()
            }.toList()
    }

//    fun enrollmentEvents(enrollment: Enrollment): List<RuleEvent>? {
//        return d2.eventModule().events().byEnrollmentUid().eq(enrollment.uid())
//            .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
//            .byEventDate().beforeOrEqual(Date())
//            .withTrackedEntityDataValues()
//            .blockingGet()
//            .map { event ->
//                RuleEvent.builder()
//                    .event(event.uid())
//                    .programStage(event.programStage())
//                    .programStageName(
//                        d2.programModule().programStages().uid(event.programStage())
//                            .blockingGet()!!.name()
//                    )
//                    .status(
//                        if (event.status() == EventStatus.VISITED) {
//                            RuleEvent.Status.ACTIVE
//                        } else {
//                            RuleEvent.Status.valueOf(event.status()!!.name)
//                        }
//                    )
//                    .eventDate(event.eventDate())
//                    .dueDate(if (event.dueDate() != null) event.dueDate() else event.eventDate())
//                    .organisationUnit(event.organisationUnit())
//                    .organisationUnitCode(
//                        d2.organisationUnitModule()
//                            .organisationUnits().uid(event.organisationUnit())
//                            .blockingGet()!!.code()
//                    )
//                    .dataValues(
//                        event.trackedEntityDataValues()?.toRuleDataValue(
//                            event,
//                            d2.dataElementModule().dataElements(),
//                            d2.programModule().programRuleVariables(),
//                            d2.optionModule().options()
//                        )
//                    )
//                    .build()
//            }.toList()
//    }

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
            .one() // buggy - doesn't return the topmost item for whatever reason
            .get()
    }

    private fun events(teiUid: String): Single<MutableList<Event>>? {
        return d2.eventModule()
            .events()
            .byTrackedEntityInstanceUids(Collections.singletonList(teiUid))
            .byDeleted().isFalse
            .orderByLastUpdated(RepositoryScope.OrderByDirection.DESC)
            .withTrackedEntityDataValues()
            .get()
    }

    private fun dataValues(
        teiUid: String,
        qty: String?,
        programStage: String,
        transaction: Transaction,
        eventDate: Date,
//        eventUid: String
    ): List<RuleDataValue> {
//        d2.eventModule().events().byUid().eq(eventUid).one()
//        val values = events(teiUid)?.map {
//            val event = it.first()
//            if (event.trackedEntityDataValues() != null) {
//                event.trackedEntityDataValues()!!.toRuleDataValue(
//                    event,
//                    d2.dataElementModule().dataElements(),
//                    d2.programModule().programRuleVariables(),
//                    d2.optionModule().options()
//                )
//            } else {
//                listOf()
//            }
//        }?.blockingGet()?.toMutableList() ?: mutableListOf()

        val values = mutableListOf<RuleDataValue>()

        // Add the quantity if defined, and valid (signs (+/-) could come as streams if incomplete)
        if (qty != null && NumberUtils.isCreatable(qty)) {
            val deUid = ConfigUtils.getTransactionDataElement(transaction.transactionType, appConfig)
            values.add(RuleDataValue.create(eventDate, programStage, deUid, qty))

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

    private fun entryDataValues(
        qty: String?,
        programStage: String,
        transaction: Transaction,
        eventDate: Date
    ): List<RuleDataValue> {
        val values = mutableListOf<RuleDataValue>()

        // Add the quantity if defined, and valid (signs (+/-) could come as streams if incomplete)
        if (qty != null && NumberUtils.isCreatable(qty)) {
            val deUid = ConfigUtils.getTransactionDataElement(transaction.transactionType, appConfig)
            values.add(RuleDataValue.create(eventDate, programStage, deUid, qty))

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

    private fun queryDataValues(eventUid: String): Flowable<List<RuleDataValue>> {
        return d2.eventModule().events().uid(eventUid).get()
            .flatMap { event: Event ->
                d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).byValue()
                    .isNotNull.get().map {
                        it.toRuleDataValue(
                            event,
                            d2.dataElementModule().dataElements(),
                            d2.programModule().programRuleVariables(),
                            d2.optionModule().options()
                        )
                    }
            }.toFlowable()
    }
}



