package com.baosystems.icrc.psm.utils

import org.hisp.dhis.rules.models.*
import timber.log.Timber

const val MAX_LEN = 120

fun debugRuleEngine(
    rules: List<Rule>,
    ruleVariables: List<RuleVariable>,
    events: List<RuleEvent>
) {
    val buffer = StringBuilder()
    buffer.append("-----                       Rules Engine dump                -----------")
    buffer.append("\n\n")
    printSeparator(buffer)
    printRuleEngineData(buffer, "Rules:")
    printSeparator(buffer)
    rules.forEach { rule ->
        printRuleEngineData(buffer, "uid:               ${rule.uid()}")
        printRuleEngineData(buffer, "name:              ${rule.name()}")
        printRuleEngineData(buffer, "condition:         ${rule.condition()}")
        printRuleEngineData(buffer, "actions:")
        rule.actions().forEach { action ->
            printRuleEngineData(buffer, "   type:           ${action.javaClass.simpleName}")
            if (action is RuleActionAssign) {
                printRuleEngineData(buffer, "   field:          ${action.field()}")
            }
            printRuleEngineData(buffer, "   data:           ${action.data()}")
            printEmpty(buffer)
        }
    }

    printSeparator(buffer)
    printRuleEngineData(buffer, "Variables:")
    ruleVariables.forEach {
        printRuleEngineData(buffer, "name:               ${it.name()}")

        if (it is RuleVariableCurrentEvent) {
            printRuleEngineData(buffer, "dataElement:        ${it.dataElement()}")
        }

        printEmpty(buffer)
    }

    printSeparator(buffer)
    printRuleEngineData(buffer, "Events:")
    printSeparator(buffer)
    events.forEach {
        printRuleEngineData(buffer, "uid:               ${it.event()}")
        printRuleEngineData(buffer, "status:              ${it.status()}")
        printRuleEngineData(buffer, "eventDate:         ${it.eventDate()}")
        printRuleEngineData(buffer, "data values:")
        it.dataValues().forEach { dv ->
            printRuleEngineData(buffer, "   dataElement:           ${dv.dataElement()}")
            printRuleEngineData(buffer, "   value:           ${dv.value()}")
            printEmpty(buffer)
        }
    }
    printSeparator(buffer)
    buffer.append("\n\n")
    Timber.d(buffer.toString())
}

fun printRuleEngineData(buffer: StringBuilder, data: String) {
    val charsLen = data.length
    val desiredLen = MAX_LEN - charsLen - 2
    println(data)
    buffer.append("|" + data.padEnd(if (desiredLen > MAX_LEN) desiredLen else MAX_LEN) + "|\n")
}

fun printSeparator(buffer: StringBuilder) {
    buffer.append("".padEnd(MAX_LEN + 2, '-') + "\n")
}

fun printEmpty(buffer: StringBuilder) {
    buffer.append("|" + "".padEnd(MAX_LEN) + "|\n")
}