package com.example.pawpatrol.notedetails

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pawpatrol.R
import com.example.pawpatrol.data.FirebaseDatabaseHolder
import com.example.pawpatrol.missing.MissingPetNote
import com.example.pawpatrol.navigation.DefaultNavigator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

private const val ARG_AUTHOR_ID = "author_id"
private const val ARG_NOTE_ID = "note_id"

class NoteDetailsFragment : Fragment(R.layout.fragment_note_details) {

    companion object {

        val TAG = "NoteDetailsFragment"

        fun newInstance(authorId: String, noteId: String): NoteDetailsFragment {
            return NoteDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_AUTHOR_ID, authorId)
                    putString(ARG_NOTE_ID, noteId)
                }
            }
        }
    }

    private var dbRef: DatabaseReference? = null
    private var dbListener: ValueEventListener? = null

    private lateinit var noteDescriptionTextView: TextView
    private lateinit var reportButton: Button
    private lateinit var deleteButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        noteDescriptionTextView = view.findViewById(R.id.note_description)
        reportButton = view.findViewById(R.id.report_button)
        deleteButton = view.findViewById(R.id.delete_button)
    }

    override fun onResume() {
        super.onResume()

        val args = requireArguments()
        val authorId = args.getString(ARG_AUTHOR_ID)!!
        val noteId = args.getString(ARG_NOTE_ID)!!

        dbRef = FirebaseDatabaseHolder.missingPet(authorId, noteId).apply {
            dbListener = addValueEventListener(object : ValueEventListener {

                override fun onDataChange(noteSnapshot: DataSnapshot) {
                    MissingPetNote.fromJson(
                        userId = authorId,
                        noteId = noteId,
                        json = noteSnapshot.value as Map<String, Any?>
                    )?.let { note ->
                        render(note)
                    }
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

    private fun render(note: MissingPetNote) {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val isAuthor = note.noteCreatorUid == currentUser.uid
        if (isAuthor) {
            reportButton.visibility = View.GONE
            deleteButton.visibility = View.VISIBLE

            reportButton.setOnClickListener(null)
            deleteButton.setOnClickListener {
                dbRef?.removeValue()
                DefaultNavigator.getInstance(this).navigateBack()
            }
        } else {
            reportButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE

            reportButton.setOnClickListener {
                DefaultNavigator.getInstance(this).navigateToReportCreation(note.noteUid)
            }
            deleteButton.setOnClickListener(null)
        }

        requireActivity().actionBar?.title = note.petName
        noteDescriptionTextView.text = note.description
    }
}
