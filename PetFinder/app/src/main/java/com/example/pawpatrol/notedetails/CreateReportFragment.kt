package com.example.pawpatrol.notedetails

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R
import com.example.pawpatrol.app.PawApplication
import com.example.pawpatrol.data.FirebaseHolder
import com.example.pawpatrol.navigation.DefaultNavigator
import com.example.pawpatrol.util.ImageSource
import com.example.pawpatrol.util.RemoveErrorWhenEditTextChanges
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

private const val ARG_NOTE_ID = "note_id"

private const val STATE_ATTACHED_PHOTOS = "attached_photos"

class CreateReportFragment : Fragment(R.layout.fragment_create_report) {

    companion object {

        const val TAG = "CreateReportFragment"

        fun newInstance(noteId: String): CreateReportFragment {
            return CreateReportFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTE_ID, noteId)
                }
            }
        }
    }

    private lateinit var reportDescriptionInputLayout: TextInputLayout
    private lateinit var reportDescriptionEditText: EditText
    private lateinit var attachedPhotosRecyclerView: RecyclerView
    private lateinit var attachPhotoButton: Button
    private lateinit var sendReportButton: Button

    private val attachedPhotos: MutableList<ImageSource> = mutableListOf()

    private val attachedPhotosAdapter = AttachedPhotosAdapter(
        glide = PawApplication.instance.glide,
        showDetach = true,
        onDeletePhoto = { _, position ->
            removeAttachedPhoto(position)
        }
    )

    private val pickImageTask = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Timber.d("Picked image uri: $it")
        if (it != null) {
            attachPhoto(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        savedInstanceState?.getStringArrayList(STATE_ATTACHED_PHOTOS)?.let { savedUris ->
            attachedPhotos.addAll(savedUris.map { ImageSource.Uri(Uri.parse(it)) })
        }
        attachedPhotosAdapter.updateImages(attachedPhotos)

        reportDescriptionInputLayout = view.findViewById(R.id.report_description_input_layout)
        reportDescriptionEditText = view.findViewById(R.id.report_description_edit_text)
        attachedPhotosRecyclerView = view.findViewById(R.id.attached_photos_recycler_view)
        attachPhotoButton = view.findViewById(R.id.attach_photo_button)
        sendReportButton = view.findViewById(R.id.send_report_button)

        reportDescriptionEditText.addTextChangedListener(
            RemoveErrorWhenEditTextChanges(reportDescriptionInputLayout)
        )

        attachedPhotosRecyclerView.apply {
            setHasFixedSize(true)
            visibility = if (attachedPhotos.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
                View.VISIBLE
            }
            adapter = attachedPhotosAdapter
        }

        attachPhotoButton.setOnClickListener {
            pickImageTask.launch("image/*")
        }

        val noteId = requireArguments().getString(ARG_NOTE_ID)!!
        sendReportButton.setOnClickListener {
            val reportDraft = composeReportDraft(noteId) ?: return@setOnClickListener

            setInputEnabled(false)
            uploadReport(reportDraft)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val urisToSave = attachedPhotos.mapTo(ArrayList()) { it.toString() }
        outState.putStringArrayList(STATE_ATTACHED_PHOTOS, urisToSave)
    }

    private fun setInputEnabled(enabled: Boolean) {
        reportDescriptionInputLayout.isEnabled = enabled
        attachedPhotosRecyclerView.isEnabled = enabled
        attachPhotoButton.isEnabled = enabled
        sendReportButton.isEnabled = enabled
    }

    private fun attachPhoto(photoUri: Uri) {
        attachedPhotosRecyclerView.visibility = View.VISIBLE
        attachedPhotos.add(ImageSource.Uri(photoUri))
        attachedPhotosAdapter.updateImages(attachedPhotos)
    }

    private fun removeAttachedPhoto(position: Int) {
        attachedPhotos.removeAt(position)
        attachedPhotosAdapter.updateImages(attachedPhotos)
        if (attachedPhotos.isEmpty()) {
            attachedPhotosRecyclerView.visibility = View.GONE
        }
    }

    private fun composeReportDraft(noteId: String): PetReportDraft? {
        val description = reportDescriptionEditText.text.toString()
        if (description.isEmpty()) {
            reportDescriptionInputLayout.error = getString(R.string.provide_description)
            return null
        }

        return PetReportDraft(
            noteUuid = noteId,
            description = description,
        )
    }

    private fun uploadReport(draft: PetReportDraft) {
        setInputEnabled(true)

        if (attachedPhotos.isNotEmpty()) {
            val uploadedImageUuids = ArrayList<String>()
            val imageTasksEndedCounter = AtomicInteger(attachedPhotos.count())
            attachedPhotos.mapNotNull { (it as? ImageSource.Uri)?.uri }.forEach { uri ->
                val imageUuid = UUID.randomUUID().toString()
                FirebaseHolder.storageForImage(imageUuid)
                    .putFile(uri)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Timber.d("Uploaded image #${imageUuid} [$uri] successfully")
                            uploadedImageUuids.add(imageUuid)
                        } else {
                            Timber.w(
                                task.exception,
                                "Failed to upload image: #%s [%s]",
                                imageUuid,
                                uri
                            )
                        }
                        if (imageTasksEndedCounter.decrementAndGet() == 0) {
                            if (uploadedImageUuids.isEmpty()) {
                                notifyImagesUploadFailure()
                            } else {
                                sendReport(draft, uploadedImageUuids)
                            }
                        }
                    }
            }
        } else {
            sendReport(draft, emptyList())
        }
    }

    private fun sendReport(draft: PetReportDraft, uploadedImageUuids: List<String>) {
        val json = mapOf(
            PetReportKeys.CREATED_AT.value to Calendar.getInstance().timeInMillis,
            PetReportKeys.DESCRIPTION.value to draft.description,
            PetReportKeys.ATTACHED_IMAGES.value to uploadedImageUuids,
        )
        FirebaseHolder.reportsForNote(draft.noteUuid)
            .push()
            .setValue(json)
            .addOnCompleteListener(requireActivity()) { createReportTask ->
                if (createReportTask.isSuccessful) {
                    Timber.d("Report sent!")
                    Snackbar.make(
                        requireView(),
                        getString(R.string.report_sent),
                        Snackbar.LENGTH_LONG
                    ).show()
                    requireView().postDelayed({
                        DefaultNavigator.getInstance(this).navigateBack()
                    }, 300L)
                } else {
                    Timber.w(createReportTask.exception, "Failed to send report!")
                    createReportTask.exception?.let {
                        Snackbar.make(
                            requireView(),
                            it.localizedMessage ?: getString(R.string.failed_to_send_report),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    setInputEnabled(true)
                }
            }
    }

    private fun notifyImagesUploadFailure() {
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_upload_image),
            Snackbar.LENGTH_LONG
        ).show()
        setInputEnabled(true)
    }
}
