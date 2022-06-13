package com.example.pawpatrol.missing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import com.example.pawpatrol.R
import com.example.pawpatrol.app.PawApplication
import com.example.pawpatrol.data.FirebaseHolder
import com.example.pawpatrol.navigation.DefaultNavigator
import com.example.pawpatrol.util.ImageSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


private const val STATE_FILE_TO_UPLOAD = "file_to_upload"
private const val STATE_EXISTING_IMAGE_UUID = "existing_image_uuid"
private const val STATE_PET_LAT = "pet_lat"
private const val STATE_PET_LNG = "pet_lng"

private const val ARG_NOTE_TO_EDIT = "note"

class MissingPetNoteCreationFragment : Fragment(R.layout.fragment_add_note), LocationListenerCompat,
    GoogleMap.OnMapClickListener {

    companion object {

        const val TAG = "MissingPetNoteCreationFragment"

        fun newInstance(): MissingPetNoteCreationFragment {
            return MissingPetNoteCreationFragment()
        }

        fun newInstance(note: MissingPetNote): MissingPetNoteCreationFragment {
            return MissingPetNoteCreationFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_NOTE_TO_EDIT, note)
                }
            }
        }
    }

    private lateinit var petNameInput: TextInputLayout
    private lateinit var petNameEditText: EditText
    private lateinit var descriptionInput: TextInputLayout
    private lateinit var descriptionEditText: EditText
    private lateinit var attachedImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var createButton: Button

    private var googleMap: GoogleMap? = null
    private var aproximatePetPosition: LatLng? = null
    private var aproximatePetPositionMarker: Marker? = null

    private var attachedImage: ImageSource? = null

    private val performOnPause: MutableList<() -> Unit> = mutableListOf()

    private val getImageTask = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        Timber.d("Image uri: $imageUri")
        attachedImage = imageUri?.let { ImageSource.Uri(it) }
    }

    @SuppressLint("MissingPermission")
    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        Timber.d("Location permission granted: %s", it)
        processLocationPermissionResult(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            with(savedInstanceState) {
                attachedImage =
                    getParcelable<Uri>(STATE_FILE_TO_UPLOAD)?.let { ImageSource.Uri(it) }
                        ?: getString(STATE_EXISTING_IMAGE_UUID)?.let { ImageSource.Firebase(it) }

                val lat = getDouble(STATE_PET_LAT, Double.NaN)
                val lng = getDouble(STATE_PET_LNG, Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    aproximatePetPosition = LatLng(lat, lng)
                }
            }
        } else {
            arguments?.getParcelable<MissingPetNote>(ARG_NOTE_TO_EDIT)?.apply {
                attachedImage = imageUuid?.let { ImageSource.Firebase(it) }
                aproximatePetPosition = location
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        petNameInput = view.findViewById(R.id.pet_name_input)
        petNameEditText = view.findViewById(R.id.pet_name_edit_text)
        descriptionInput = view.findViewById(R.id.description_input)
        descriptionEditText = view.findViewById(R.id.description_edit_text)
        attachedImageView = view.findViewById(R.id.attached_photo_image_view)
        uploadImageButton = view.findViewById(R.id.upload_image_button)
        createButton = view.findViewById(R.id.create_button)

        uploadImageButton.setOnClickListener {
            getImageTask.launch("image/*")
        }

        if (arguments != null) {
            createButton.setText(R.string.edit_note)
        }
        createButton.setOnClickListener {
            val petName = petNameEditText.text.toString()
            if (petName.isBlank()) {
                petNameInput.error = getString(R.string.provide_pet_name)
                return@setOnClickListener
            }

            val description = descriptionEditText.text.toString()
            if (description.isBlank()) {
                descriptionInput.error = getString(R.string.provide_description)
                return@setOnClickListener
            }

            val attachedImage = this.attachedImage
            if (attachedImage == null) {
                Snackbar.make(
                    view,
                    R.string.please_attach_image,
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            setInputsEnabled(false)

            val location = aproximatePetPosition
            val note: MissingPetNote? = arguments?.getParcelable(ARG_NOTE_TO_EDIT)
            uploadImageIfRequired(attachedImage) { imageId ->
                createOrEdit(
                    note,
                    petName,
                    description,
                    imageId,
                    location
                )
            }
        }

        if (savedInstanceState == null) {
            arguments?.getParcelable<MissingPetNote>(ARG_NOTE_TO_EDIT)?.apply {
                petNameEditText.setText(petName)
                descriptionEditText.setText(description)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        requireActivity().actionBar?.title = getString(
            arguments
                ?.let { R.string.edit_note }
                ?: R.string.add_note
        )

        (childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment)?.let {
            it.getMapAsync { googleMap ->
                Timber.d("Map ready: %s", googleMap)
                this.googleMap = googleMap

                aproximatePetPosition?.let { petPosition ->
                    aproximatePetPositionMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(petPosition)
                            .title(getString(R.string.pet_position))
                    )

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(petPosition, 6F))
                }

                googleMap.setOnMapClickListener(this)
            }
        }

        val permissionResult = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionResult == PackageManager.PERMISSION_GRANTED) {
            listenForLocation()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        loadAttachedImage(attachedImage)
    }

    override fun onPause() {
        super.onPause()

        performOnPause.forEach {
            it.invoke()
        }
        performOnPause.clear()

        aproximatePetPositionMarker?.remove()
        aproximatePetPositionMarker = null

        PawApplication.instance.glide.clear(attachedImageView)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        attachedImage?.let { image ->
            when (image) {
                is ImageSource.Firebase -> {
                    outState.putString(STATE_EXISTING_IMAGE_UUID, image.uuid)
                }
                is ImageSource.Uri -> {
                    outState.putParcelable(STATE_FILE_TO_UPLOAD, image.uri)
                }
            }
        }

        aproximatePetPosition?.let {
            outState.putDouble(STATE_PET_LAT, it.latitude)
            outState.putDouble(STATE_PET_LNG, it.longitude)
        }

        super.onSaveInstanceState(outState)
    }

    private fun setInputsEnabled(enabled: Boolean) {
        petNameInput.isEnabled = enabled
        descriptionInput.isEnabled = enabled
        uploadImageButton.isEnabled = enabled
        createButton.isEnabled = enabled
    }

    private fun loadAttachedImage(attachedImage: ImageSource?) {
        if (attachedImage != null) {
            attachedImageView.visibility = View.VISIBLE
            PawApplication.instance.glide
                .load(attachedImage.toTarget())
                .into(attachedImageView)
        } else {
            PawApplication.instance.glide.clear(attachedImageView)
            attachedImageView.setImageDrawable(null)
            attachedImageView.visibility = View.GONE
        }
    }

    @SuppressLint("MissingPermission")
    private fun processLocationPermissionResult(granted: Boolean) {
        if (granted) {
            listenForLocation()
        }
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
        Timber.d("Location changed: %s", location)
        if (aproximatePetPosition != null) {
            return
        }

        val gmsLocation = LatLng(location.latitude, location.longitude)
        googleMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(gmsLocation, 12F))
        }
    }

    override fun onMapClick(clickLocation: LatLng) {
        googleMap?.apply {
            aproximatePetPosition = clickLocation
            aproximatePetPositionMarker?.remove()
            aproximatePetPositionMarker = addMarker(
                MarkerOptions()
                    .position(clickLocation)
                    .title(getString(R.string.pet_position))
            )
        }
    }

    private fun uploadImageIfRequired(imageSource: ImageSource, onSuccess: (String) -> Unit) {
        when (imageSource) {
            is ImageSource.Firebase -> {
                onSuccess(imageSource.uuid)
            }
            is ImageSource.Uri -> {
                val imageUuid = UUID.randomUUID().toString()
                FirebaseHolder.storageForImage(imageUuid)
                    .putFile(imageSource.uri)
                    .addOnCompleteListener(requireActivity()) { uploadResult ->
                        if (uploadResult.isSuccessful) {
                            Timber.d("Uploaded image successfully")
                            onSuccess(imageUuid)
                        } else {
                            setInputsEnabled(true)
                            uploadResult.exception?.let {
                                Snackbar.make(
                                    requireView(),
                                    it.localizedMessage
                                        ?: getString(R.string.failed_to_upload_image),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }
    }

    private fun onSubmitted(
        @StringRes onSuccessText: Int,
        @StringRes onFailureFallbackText: Int,
    ) = OnCompleteListener<Void> { task ->
        if (task.isSuccessful) {
            Snackbar.make(
                requireView(),
                getString(onSuccessText),
                Snackbar.LENGTH_LONG
            ).show()
            requireView().postDelayed(200L) {
                DefaultNavigator.getInstance(this).navigateBack()
            }
        } else {
            setInputsEnabled(true)
            task.exception?.let {
                Snackbar.make(
                    requireView(),
                    it.localizedMessage
                        ?: getString(onFailureFallbackText),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createOrEdit(
        note: MissingPetNote?,
        petName: String,
        description: String,
        tmpImageUuid: String,
        location: LatLng?,
    ) {
        val firebaseReference = FirebaseHolder.missingPets.run {
            if (note != null) {
                child(note.noteCreatorUid)
                    .child(note.noteUid)
            } else {
                child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .push()
            }
        }

        val callback = if (note != null) {
            onSubmitted(R.string.note_edited, R.string.failed_to_edit_note)
        } else {
            onSubmitted(R.string.note_created, R.string.failed_to_create_note)
        }

        firebaseReference
            .setValue(
                mapOf(
                    MissingPetNoteKeys.NAME.value to petName,
                    MissingPetNoteKeys.DESCRIPTION.value to description,
                    MissingPetNoteKeys.CREATED_AT.value to Calendar.getInstance().timeInMillis,
                    MissingPetNoteKeys.IMAGE_UUID.value to tmpImageUuid,
                    MissingPetNoteKeys.LAT.value to location?.latitude,
                    MissingPetNoteKeys.LNG.value to location?.longitude,
                )
            )
            .addOnCompleteListener(requireActivity(), callback)
    }
}
