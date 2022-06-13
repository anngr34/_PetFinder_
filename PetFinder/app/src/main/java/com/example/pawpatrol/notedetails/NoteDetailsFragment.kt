package com.example.pawpatrol.notedetails

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pawpatrol.R
import com.example.pawpatrol.app.PawApplication
import com.example.pawpatrol.data.FirebaseHolder
import com.example.pawpatrol.missing.MissingPetNote
import com.example.pawpatrol.navigation.DefaultNavigator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
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

        const val TAG = "NoteDetailsFragment"

        fun newInstance(authorId: String, noteId: String): NoteDetailsFragment {
            return NoteDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_AUTHOR_ID, authorId)
                    putString(ARG_NOTE_ID, noteId)
                }
            }
        }
    }

    private val performOnPause: MutableList<() -> Unit> = mutableListOf()

    private lateinit var attachedPetImageView: ImageView
    private lateinit var mapFragmentView: View
    private lateinit var noteDescriptionTextView: TextView
    private lateinit var reportButton: Button
    private lateinit var seeReportsButton: Button
    private lateinit var deleteButton: Button

    private var dbRef: DatabaseReference? = null

    private var menu: Menu? = null

    private var loadedNote: MissingPetNote? = null

    private fun acquireMap(onMapReady: (GoogleMap) -> Unit) {
        (childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment)?.let {
            it.getMapAsync { googleMap ->
                Timber.d("Map ready: %s", googleMap)
                onMapReady(googleMap)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        inflater.inflate(R.menu.details_fragment_menu, menu)
        Timber.d("onCreateOptionsMenu")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        attachedPetImageView = view.findViewById(R.id.pet_image_view)
        mapFragmentView = view.findViewById(R.id.map_fragment)
        noteDescriptionTextView = view.findViewById(R.id.note_description)
        reportButton = view.findViewById(R.id.report_button)
        seeReportsButton = view.findViewById(R.id.see_reports)
        deleteButton = view.findViewById(R.id.delete_button)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.edit_menu_item) {
            val note = loadedNote
            return if (note != null) {
                DefaultNavigator.getInstance(this).navigateToNoteEdit(note)
                true
            } else {
                false
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        val args = requireArguments()
        val authorId = args.getString(ARG_AUTHOR_ID)!!
        val noteId = args.getString(ARG_NOTE_ID)!!

        dbRef = FirebaseHolder.missingPet(authorId, noteId).apply {
            val dataListener = object : ValueEventListener {

                override fun onDataChange(noteSnapshot: DataSnapshot) {
                    (noteSnapshot.value as? Map<String, Any?>)
                        ?.let { json ->
                            MissingPetNote.fromJson(
                                userId = authorId,
                                noteId = noteId,
                                json = json
                            )
                        }
                        ?.let { render(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.w(error.message)
                }
            }
            addValueEventListener(dataListener)
            performOnPause.add {
                removeEventListener(dataListener)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        dbRef = null

        performOnPause.forEach { task ->
            task.invoke()
        }
        performOnPause.clear()

        PawApplication.instance.glide.clear(attachedPetImageView)
    }

    private fun render(note: MissingPetNote) {
        loadedNote = note
        note.imageUuid?.let {
            PawApplication.instance.glide.load(FirebaseHolder.imagesStorageRef.child(it))
                .into(attachedPetImageView)
        }

        val editMenuItem = menu?.findItem(R.id.edit_menu_item)
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val isAuthor = note.noteCreatorUid == currentUser.uid
        if (isAuthor) {
            editMenuItem?.isVisible = true

            reportButton.visibility = View.GONE
            seeReportsButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE

            reportButton.setOnClickListener(null)
            seeReportsButton.setOnClickListener {
                DefaultNavigator.getInstance(this).navigateToReports(note.noteUid)
            }
            deleteButton.setOnClickListener {
                dbRef?.removeValue()
                DefaultNavigator.getInstance(this).navigateBack()
            }
        } else {
            editMenuItem?.isVisible = false

            reportButton.visibility = View.VISIBLE
            seeReportsButton.visibility = View.GONE
            deleteButton.visibility = View.GONE

            reportButton.setOnClickListener {
                DefaultNavigator.getInstance(this).navigateToReportCreation(note.noteUid)
            }
            seeReportsButton.setOnClickListener(null)
            deleteButton.setOnClickListener(null)
        }

        requireActivity().actionBar?.title = note.petName
        noteDescriptionTextView.text = note.description

        val approxLoc = note.location
        if (approxLoc != null) {
            mapFragmentView.visibility = View.VISIBLE
            acquireMap { map ->
                map.addMarker(
                    MarkerOptions()
                        .position(approxLoc)
                        .title(getString(R.string.pet_position))
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(approxLoc, 6F))
            }
        } else {
            mapFragmentView.visibility = View.GONE
        }
    }
}
