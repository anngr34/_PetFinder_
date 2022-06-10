package com.example.pawpatrol.missing

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import com.example.pawpatrol.R
import com.example.pawpatrol.data.FirebaseDatabaseHolder
import com.example.pawpatrol.navigation.DefaultNavigator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class MissingPetNoteCreationFragment : Fragment(R.layout.fragment_add_note) {

    companion object {
        val TAG = "MissingPetNoteCreationFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val petNameInput: TextInputLayout = view.findViewById(R.id.pet_name_input)
        val petNameEditText: EditText = view.findViewById(R.id.pet_name_edit_text)
        val descriptionInput: TextInputLayout = view.findViewById(R.id.description_input)
        val descriptionEditText: EditText = view.findViewById(R.id.description_edit_text)
        val uploadImageButton: Button = view.findViewById(R.id.upload_image_button)
        val createButton: Button = view.findViewById(R.id.create_button)

        fun setInputsEnabled(enabled: Boolean) {
            petNameInput.isEnabled = enabled
            descriptionInput.isEnabled = enabled
            uploadImageButton.isEnabled = enabled
            createButton.isEnabled = enabled
        }

        createButton.setOnClickListener {
            val petName = petNameEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (petName.isBlank()) {
                petNameInput.error = getString(R.string.provide_pet_name)
                return@setOnClickListener
            }
            if (description.isBlank()) {
                descriptionInput.error = getString(R.string.provide_description)
                return@setOnClickListener
            }

            setInputsEnabled(false)

            FirebaseDatabaseHolder.missingPets
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(UUID.randomUUID().toString())
                .setValue(
                    mapOf(
                        MissingPetNoteKeys.NAME.value to petName,
                        MissingPetNoteKeys.DESCRIPTION.value to description,
                        MissingPetNoteKeys.CREATED_AT.value to Calendar.getInstance().timeInMillis,
                    )
                )
                .addOnCompleteListener(requireActivity()) { result ->
                    if (result.isSuccessful) {
                        Snackbar.make(
                            view,
                            getString(R.string.note_created),
                            Snackbar.LENGTH_LONG
                        ).show()
                        view.postDelayed(200L) {
                            DefaultNavigator.getInstance(this).navigateBack()
                        }
                    } else {
                        setInputsEnabled(true)
                        result.exception?.let {
                            Snackbar.make(
                                view,
                                it.localizedMessage ?: getString(R.string.failed_to_create_note),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().actionBar?.title = getString(R.string.add_note)
    }
}
