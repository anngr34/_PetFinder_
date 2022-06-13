package com.example.pawpatrol.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R
import com.example.pawpatrol.app.PawApplication
import com.example.pawpatrol.data.FirebaseHolder
import com.example.pawpatrol.missing.MissingPetNote
import com.example.pawpatrol.missing.NotesAdapter
import com.example.pawpatrol.navigation.DefaultNavigator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    companion object {
        const val TAG = "ProfileFragment"
    }

    private val selfNotesAdapter = NotesAdapter(
        glide = PawApplication.instance.glide,
        navigateToNoteDetails = { authorId, noteId ->
            DefaultNavigator.getInstance(this).navigateToNoteDetails(authorId, noteId)
        }
    )

    private var dbRef: DatabaseReference? = null
    private var dbListener: ValueEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val logout: View = view.findViewById(R.id.logout)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }

        view.findViewById<RecyclerView>(R.id.notes_list).also {
            it.adapter = selfNotesAdapter
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().actionBar?.title = getString(R.string.profile)

        dbRef = FirebaseHolder.missingPetsForProfile.apply {
            dbListener = addValueEventListener(object : ValueEventListener {

                override fun onDataChange(missingPetsForProfileSnapshot: DataSnapshot) {
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    val notes =
                        missingPetsForProfileSnapshot.children.mapNotNull { missingNoteSnapshot ->
                            val noteId: String = missingNoteSnapshot.key ?: return@mapNotNull null
                            (missingNoteSnapshot.value as? Map<String, Any?>)?.let {
                                MissingPetNote.fromJson(
                                    userId = userId,
                                    noteId = noteId,
                                    json = it
                                )
                            }
                        }

                    selfNotesAdapter.update(notes)
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.w(error.message)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()

        val ref = dbRef
        val listener = dbListener
        if (ref != null && listener != null) {
            ref.removeEventListener(listener)
            dbRef = null
            dbListener = null
        }
    }
}
