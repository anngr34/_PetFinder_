package com.example.pawpatrol.missing

import timber.log.Timber

data class MissingPetNote(
    val noteUid: String,
    val noteCreatorUid: String,
    val petName: String,
    val description: String,
    val createdAt: Long,
) {

    companion object {

        fun fromJson(userId: String, noteId: String, json: Map<String, Any?>): MissingPetNote? {
            val name: String =
                (json[MissingPetNoteKeys.NAME.value] as? String) ?: return null
            Timber.d("name: $name")
            val description: String =
                (json[MissingPetNoteKeys.DESCRIPTION.value] as? String) ?: return null
            Timber.d("description: $description")
            val createdAt: Long =
                (json[MissingPetNoteKeys.CREATED_AT.value] as? Long) ?: return null
            Timber.d("createdAt: $createdAt")
            return MissingPetNote(
                noteUid = noteId,
                noteCreatorUid = userId,
                petName = name,
                description = description,
                createdAt = createdAt,
            )
        }
    }
}
