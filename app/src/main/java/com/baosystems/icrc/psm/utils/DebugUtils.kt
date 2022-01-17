package com.baosystems.icrc.psm.utils

import org.hisp.dhis.rules.models.*
import timber.log.Timber

fun debugRuleEngine(
    rules: List<Rule>,
    ruleVariables: List<RuleVariable>,
    events: List<RuleEvent>
) {
    val strLen = 120
    val buffer = StringBuilder()
    buffer.append("-----                       Rules Engine dump                -----------")
    buffer.append("\n\n")
    printSeparator(buffer, strLen)
    printRuleEngineData(buffer, "Rules:", strLen)
    printSeparator(buffer, strLen)
    rules.forEach { rule ->
        printRuleEngineData(buffer, "uid:               ${rule.uid()}", strLen)
        printRuleEngineData(buffer, "name:              ${rule.name()}", strLen)
        printRuleEngineData(buffer, "condition:         ${rule.condition()}", strLen)
        printRuleEngineData(buffer, "actions:", strLen)
        rule.actions().forEach { action ->
            printRuleEngineData(buffer, "   type:           ${action.javaClass.simpleName}", strLen)
            if (action is RuleActionAssign) {
                printRuleEngineData(buffer, "   field:          ${action.field()}", strLen)
            }
            printRuleEngineData(buffer, "   data:           ${action.data()}", strLen)
            printEmpty(buffer, strLen)
        }
    }

    printSeparator(buffer, strLen)
    printRuleEngineData(buffer, "Variables:", strLen)
    ruleVariables.forEach {
        printRuleEngineData(buffer, "name:               ${it.name()}", strLen)

        if (it is RuleVariableCurrentEvent) {
            printRuleEngineData(buffer, "dataElement:        ${it.dataElement()}", strLen)
        }

        printEmpty(buffer, strLen)
    }

    printSeparator(buffer, strLen)
    printRuleEngineData(buffer, "Events:", strLen)
    printSeparator(buffer, strLen)
    events.forEach {
        printRuleEngineData(buffer, "uid:               ${it.event()}", strLen)
        printRuleEngineData(buffer, "status:              ${it.status()}", strLen)
        printRuleEngineData(buffer, "eventDate:         ${it.eventDate()}", strLen)
        printRuleEngineData(buffer, "data values:", strLen)
        it.dataValues().forEach { dv ->
            printRuleEngineData(buffer, "   dataElement:           ${dv.dataElement()}", strLen)
            printRuleEngineData(buffer, "   value:           ${dv.value()}", strLen)
            printEmpty(buffer, strLen)
        }
    }
    printSeparator(buffer, strLen)
    buffer.append("\n\n")
    Timber.d(buffer.toString())
}

fun printRuleEngineData(buffer: StringBuilder, data: String, length: Int) {
    val charsLen = data.length
    val desiredLen = length - charsLen - 2
    println(data)
    buffer.append("|" + data.padEnd(if (desiredLen > length) desiredLen else length) + "|\n")
}

fun printSeparator(buffer: StringBuilder, length: Int) {
    buffer.append("".padEnd(length + 2, '-') + "\n")
}

fun printEmpty(buffer: StringBuilder, length: Int) {
    buffer.append("|" + "".padEnd(length) + "|\n")
}