package com.example.pawpatrol.missing

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R
import com.example.pawpatrol.data.FirebaseDatabaseHolder
import com.example.pawpatrol.navigation.DefaultNavigator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class SearchAnimalsFragment : Fragment(R.layout.fragment_search) {

    companion object {
        val TAG = "SearchAnimalsFragment"
    }

    private var dbListener: ValueEventListener? = null

    private lateinit var petsListRecyclerView: RecyclerView
    private lateinit var addNoteButton: FloatingActionButton
    private lateinit var progressBar: View
    private lateinit var stubView: View

    private val notesAdapter = NotesAdapter(
        navigateToNoteDetails = { authorId, noteId ->
            DefaultNavigator.getInstance(this).navigateToNoteDetails(authorId, noteId)
        }
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_fragment_menu, menu)
        Timber.d("onCreateOptionsMenu")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        petsListRecyclerView = view.findViewById<RecyclerView>(R.id.pets_list).also {
            it.adapter = notesAdapter
        }
        addNoteButton = view.findViewById(R.id.add_missing_animal_note)
        progressBar = view.findViewById(R.id.fetching_progress)
        stubView = view.findViewById(R.id.stub)

        addNoteButton.setOnClickListener {
            DefaultNavigator.getInstance(this).navigateToNoteCreation()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.profile_menu_item) {
            DefaultNavigator.getInstance(this).navigateToProfile()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        requireActivity().actionBar?.title = getString(R.string.missing_pets_search)

        // subscribe to missing pets updates
        dbListener = FirebaseDatabaseHolder.missingPets
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(missingPetsSnapshot: DataSnapshot) {
                    val notes = missingPetsSnapshot.children.flatMap { missingPetsPerUserSnapshot ->
                        val userId: String =
                            missingPetsPerUserSnapshot.key ?: return@flatMap emptyList()
                        missingPetsPerUserSnapshot.children.mapNotNull { missingNoteSnapshot ->
                            val noteId: String = missingNoteSnapshot.key ?: return@mapNotNull null
                            MissingPetNote.fromJson(
                                userId = userId,
                                noteId = noteId,
                                json = missingNoteSnapshot.value as Map<String, Any?>
                            )
                        }
                    }

                    Timber.d("Notes: $notes")
                    val hasNotes = notes.isNotEmpty()

                    petsListRecyclerView.visibility = if (hasNotes) View.VISIBLE else View.GONE
                    addNoteButton.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    stubView.visibility = if (hasNotes) View.GONE else View.VISIBLE

                    notesAdapter.update(notes)
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.w(error.message)
                }
            })
    }

    override fun onPause() {
        super.onPause()

        // unsubscribe from missing pets
        dbListener?.let {
            FirebaseDatabaseHolder.missingPets.removeEventListener(it)
            dbListener = null
        }
    }
}
