package com.baosystems.icrc.psm.utils

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

object AttributeHelper {
    fun teiItemName(trackedEntityInstance: TrackedEntityInstance): String? =
        trackedEntityInstance.trackedEntityAttributeValues()?.get(0)?.value()

    fun teiItemCode(trackedEntityInstance: TrackedEntityInstance) =
        trackedEntityInstance.trackedEntityAttributeValues()?.get(1)?.value()

    fun teiItemValueByAttributeUid(trackedEntityInstance: TrackedEntityInstance,
                                   attributeUid: String): String? {
        val attrValues = trackedEntityInstance.trackedEntityAttributeValues()

        return if (attrValues == null || attrValues.isEmpty()) {
            null
        } else {
            attrValues.firstOrNull {
                it.trackedEntityAttribute().equals(attributeUid)
            }?.value()
        }
    }
}