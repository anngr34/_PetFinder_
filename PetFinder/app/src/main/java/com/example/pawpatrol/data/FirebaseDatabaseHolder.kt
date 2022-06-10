package com.example.pawpatrol.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private const val DB_URL = ""

object FirebaseDatabaseHolder {

    val instance: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(DB_URL)
    }

    val missingPets: DatabaseReference by lazy {
        instance.getReference("missing_pets")
    }

    val missingPetsForProfile: DatabaseReference
        get() {
            val user = FirebaseAuth.getInstance().currentUser!!
            return missingPets.child(user.uid)
        }

    fun missingPet(authorId: String, noteId: String): DatabaseReference {
        return missingPets.child(authorId).child(noteId)
    }
}
