package com.baosystems.icrc.psm.utils

import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleVariable

class RuleEngineHelper {
    companion object {
        @JvmStatic
        fun getRuleEngine(rules: List<Rule>,
                          ruleVariables: List<RuleVariable>,
                          constants: Map<String, String>,
                          supplementaryData: Map<String, List<String>>
        ): RuleEngine {
            return RuleEngineContext.builder()
                .rules(rules)
                .ruleVariables(ruleVariables)
                .constantsValue(constants)
                .supplementaryData(supplementaryData)
                .build()
                .toEngineBuilder()
                .build()
        }
    }
}