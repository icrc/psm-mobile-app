package com.baosystems.icrc.psm.services

import com.google.common.collect.Lists
import junit.framework.Assert.assertEquals
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class ProgramRuleTests {
    private fun getRuleEngine(rules: List<Rule>): RuleEngine.Builder? {
        val ruleVariable: RuleVariable = RuleVariableCalculatedValue
            .create("test_calculated_value", "", RuleValueType.TEXT)
        return RuleEngineContext
            .builder()
            .rules(rules)
            .ruleVariables(Arrays.asList(ruleVariable))
            .supplementaryData(HashMap())
            .constantsValue(HashMap())
            .build().toEngineBuilder().triggerEnvironment(TriggerEnvironment.SERVER)
    }

    private fun createRules(i: Int): List<Rule>? {
        val rules = Lists.newArrayList<Rule>()
        val assignAction: RuleAction =
            RuleActionAssign.create("#{test_calculated_value}", "2+1", null)
        val rule = Rule
            .create(null, 1, "true", Arrays.asList(assignAction), "test_program_rule1", "")
        val sendMessageAction: RuleAction = RuleActionSendMessage.create("test_notification", "4")
        val rule2 = Rule
            .create(
                null,
                4,
                "#{test_calculated_value}==4",
                Arrays.asList(sendMessageAction),
                "test_program_rule2",
                ""
            )
        for (j in 0 until i) {
            rules.add(rule)
            rules.add(rule2)
        }
        return rules
    }

    @Test
    @Throws(Exception::class)
    fun evaluateTOneRuleTest() {
        val i = 1
        val ruleEngineBuilder = getRuleEngine(createRules(i)!!)
        val enrollment = RuleEnrollment.builder()
            .enrollment("test_enrollment")
            .programName("test_program")
            .incidentDate(Date())
            .enrollmentDate(Date())
            .status(RuleEnrollment.Status.ACTIVE)
            .organisationUnit("test_ou")
            .organisationUnitCode("test_ou_code")
            .attributeValues(Arrays.asList())
            .build()
        val ruleEvent = RuleEvent.builder()
            .event("test_event")
            .programStage("test_program_stage")
            .programStageName("")
            .status(RuleEvent.Status.ACTIVE)
            .eventDate(Date())
            .dueDate(Date())
            .organisationUnit("")
            .organisationUnitCode("")
            .dataValues(
                Arrays.asList(
                    RuleDataValue.create(
                        Date(), "test_program_stage", "test_data_element", "test_value"
                    )
                )
            )
            .build()
        val ruleEngine = ruleEngineBuilder!!.enrollment(enrollment).build()
        val ruleEffects = ruleEngine.evaluate(ruleEvent).call()
        assertEquals(ruleEffects.size, i)
    }

//    @Test
//    @Throws(Exception::class)
//    fun sendMessageMustGetValueFromAssignAction() {
//        val assignAction: RuleAction =
//            RuleActionAssign.create("#{test_calculated_value}", "2+2", null)
//        val rule = Rule
//            .create(null, 1, "true", Arrays.asList(assignAction), "test_program_rule1", "")
////        val sendMessageAction: RuleAction = RuleActionSendMessage.create("test_notification", "4")
////        val rule2 = Rule
////            .create(
////                null,
////                4,
////                "#{test_calculated_value}==4",
////                Arrays.asList(sendMessageAction),
////                "test_program_rule2",
////                ""
////            )
//        val enrollment = RuleEnrollment.builder()
//            .enrollment("test_enrollment")
//            .programName("test_program")
//            .incidentDate(Date())
//            .enrollmentDate(Date())
//            .status(RuleEnrollment.Status.ACTIVE)
//            .organisationUnit("test_ou")
//            .organisationUnitCode("test_ou_code")
//            .attributeValues(Arrays.asList())
//            .build()
//        val ruleEvent = RuleEvent.builder()
//            .event("test_event")
//            .programStage("test_program_stage")
//            .programStageName("")
//            .status(RuleEvent.Status.ACTIVE)
//            .eventDate(Date())
//            .dueDate(Date())
//            .organisationUnit("")
//            .organisationUnitCode("")
//            .dataValues(
//                Arrays.asList(
//                    RuleDataValue.create(
//                        Date(), "test_program_stage", "test_data_element", "test_value"
//                    )
//                )
//            )
//            .build()
//        val ruleEngine = getRuleEngine(Arrays.asList(rule))!!.enrollment(enrollment).build()
//        val ruleEffects = ruleEngine.evaluate(ruleEvent).call()
//        assertEquals(ruleEffects[0].data(), "4")
////        assertEquals(ruleEffects[0].ruleAction(), sendMessageAction)
//    }
}