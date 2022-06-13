package com.example.pawpatrol.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

private const val DB_URL = "https://pawpatrol-b3778-default-rtdb.europe-west1.firebasedatabase.app/"
private const val STORAGE_BASE_URL = "gs://pawpatrol-b3778.appspot.com"

object FirebaseHolder {

    val instance: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(DB_URL)
    }

    val missingPets: DatabaseReference by lazy {
        instance.getReference("missing_pets")
    }

    val missingPetReports: DatabaseReference by lazy {
        instance.getReference("reports")
    }

    val missingPetsForProfile: DatabaseReference
        get() {
            val user = FirebaseAuth.getInstance().currentUser!!
            return missingPets.child(user.uid)
        }

    fun missingPet(authorId: String, noteId: String): DatabaseReference {
        return missingPets.child(authorId).child(noteId)
    }

    val baseStorageReference by lazy {
        Firebase.storage(STORAGE_BASE_URL)
    }

    val imagesStorageRef by lazy {
        baseStorageReference.reference.child("images")
    }

    fun storageForImage(uuid: String): StorageReference {
        return imagesStorageRef.child(uuid)
    }

    fun reportsForNote(noteId: String): DatabaseReference {
        return missingPetReports.child(noteId)
    }
}
