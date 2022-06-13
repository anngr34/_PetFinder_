package com.example.pawpatrol.notedetails

import timber.log.Timber

data class PetReport(
    val reportId: String,
    val createdAt: Long,
    val description: String,
    val attachedImageIds: List<String>,
) {

    companion object {

        fun parse(reportId: String, json: Map<String, Any?>): PetReport? {
            Timber.d("Parsing report json: %s", json)
            val createdAt = json[PetReportKeys.CREATED_AT.value] as? Long ?: return null
            val description = json[PetReportKeys.DESCRIPTION.value] as? String ?: return null
            val attachedImageIds: List<String> =
                json[PetReportKeys.ATTACHED_IMAGES.value] as? List<String> ?: emptyList()

            return PetReport(
                reportId = reportId,
                createdAt = createdAt,
                description = description,
                attachedImageIds = attachedImageIds,
            )
        }
    }
}
