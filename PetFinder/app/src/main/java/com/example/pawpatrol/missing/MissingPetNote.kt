package com.example.pawpatrol.missing

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

data class MissingPetNote(
    val noteUid: String,
    val noteCreatorUid: String,
    val petName: String,
    val description: String,
    val createdAt: Long,
    val imageUuid: String?,
    val location: LatLng?,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString(),
        parcel.readParcelable(LatLng::class.java.classLoader)
    )

    companion object {

        fun fromJson(userId: String, noteId: String, json: Map<String, Any?>): MissingPetNote? {
            Timber.d("Parse json: %s", json)
            val name: String =
                (json[MissingPetNoteKeys.NAME.value] as? String) ?: return null

            val description: String =
                (json[MissingPetNoteKeys.DESCRIPTION.value] as? String) ?: return null

            val createdAt: Long =
                (json[MissingPetNoteKeys.CREATED_AT.value] as? Long) ?: return null

            val imageUuid: String? = (json[MissingPetNoteKeys.IMAGE_UUID.value] as? String)

            val lat: Double? = json[MissingPetNoteKeys.LAT.value] as? Double
            val lng: Double? = json[MissingPetNoteKeys.LNG.value] as? Double
            val location = if (lat != null && lng != null) {
                LatLng(lat, lng)
            } else {
                null
            }

            return MissingPetNote(
                noteUid = noteId,
                noteCreatorUid = userId,
                petName = name,
                description = description,
                createdAt = createdAt,
                imageUuid = imageUuid,
                location = location,
            )
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<MissingPetNote> {
            override fun createFromParcel(parcel: Parcel): MissingPetNote {
                return MissingPetNote(parcel)
            }

            override fun newArray(size: Int): Array<MissingPetNote?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(noteUid)
        parcel.writeString(noteCreatorUid)
        parcel.writeString(petName)
        parcel.writeString(description)
        parcel.writeLong(createdAt)
        parcel.writeString(imageUuid)
        parcel.writeParcelable(location, flags)
    }

    override fun describeContents(): Int {
        return 0
    }
}
