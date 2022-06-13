package com.example.pawpatrol.missing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R
import com.example.pawpatrol.app.PawApplication
import com.example.pawpatrol.data.FirebaseHolder
import com.example.pawpatrol.navigation.DefaultNavigator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class SearchAnimalsFragment : Fragment(R.layout.fragment_search), LocationListenerCompat {

    companion object {
        const val TAG = "SearchAnimalsFragment"
    }

    private lateinit var petsListRecyclerView: RecyclerView
    private lateinit var addNoteButton: FloatingActionButton
    private lateinit var progressBar: View
    private lateinit var stubView: View

    private val notesAdapter = NotesAdapter(
        glide = PawApplication.instance.glide,
        navigateToNoteDetails = { authorId, noteId ->
            DefaultNavigator.getInstance(this).navigateToNoteDetails(authorId, noteId)
        }
    )

    private val performOnPause: MutableList<() -> Unit> = mutableListOf()

    private var notes: List<MissingPetNote> = emptyList()
    private var currentLocation: Location? = null

    private val dbListener = object : ValueEventListener {
        override fun onDataChange(missingPetsSnapshot: DataSnapshot) {
            notes = missingPetsSnapshot.children.flatMap { missingPetsPerUserSnapshot ->
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
            onDataChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.w(error.message)
        }
    }

    private fun onDataChanged() {
        val lastKnownNotes = this.notes.toMutableList()
        val currentLocation = this.currentLocation

        Timber.d("Notes: $lastKnownNotes")
        val hasNotes = lastKnownNotes.isNotEmpty()

        petsListRecyclerView.visibility = if (hasNotes) View.VISIBLE else View.GONE
        addNoteButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        stubView.visibility = if (hasNotes) View.GONE else View.VISIBLE

        if (currentLocation != null) {
            Timber.d("Sort by location")
            lastKnownNotes.sortBy { note ->
                note.location?.let { petLoc ->
                    val latitudeDist = petLoc.latitude - currentLocation.latitude
                    val longitudeDist = petLoc.longitude - currentLocation.longitude
                    sqrt(latitudeDist * latitudeDist + longitudeDist * longitudeDist)
                } ?: Double.MAX_VALUE
            }
        } else {
            Timber.d("Sort by time")
            lastKnownNotes.sortByDescending { note ->
                note.createdAt
            }
        }

        notesAdapter.update(lastKnownNotes)
    }

    @SuppressLint("MissingPermission")
    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        Timber.d("Location permission granted: %s", it)
        if (it) {
            listenForLocation()
        }
    }

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

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        requireActivity().actionBar?.title = getString(R.string.missing_pets_search)

        val permissionResult = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val locationGranted = permissionResult == PackageManager.PERMISSION_GRANTED
        if (!locationGranted) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            listenForLocation()
        }

        // subscribe to missing pets updates
        FirebaseHolder.missingPets.addValueEventListener(dbListener)

        performOnPause.add {
            FirebaseHolder.missingPets.removeEventListener(dbListener)
        }
    }

    override fun onPause() {
        super.onPause()

        performOnPause.forEach { task ->
            task.invoke()
        }
        performOnPause.clear()
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    private fun listenForLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            TimeUnit.MINUTES.toMillis(5),
            100F,
            this
        )
        performOnPause.add {
            locationManager.removeUpdates(this)
        }
    }

    override fun onLocationChanged(location: Location) {
        Timber.d("Location updated: %s", location)
        currentLocation = location
        onDataChanged()
    }
}
