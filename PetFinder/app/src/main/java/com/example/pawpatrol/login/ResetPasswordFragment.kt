package com.example.pawpatrol.login

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.pawpatrol.R
import com.example.pawpatrol.navigation.DefaultNavigator
import com.example.pawpatrol.navigation.Navigator
import com.example.pawpatrol.util.AuthUtils
import com.example.pawpatrol.util.RemoveErrorWhenEditTextChanges
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordFragment : Fragment(R.layout.reset_pass_fragment) {

    companion object {
        val TAG = "ResetPasswordFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val emailInputLayout: TextInputLayout = view.findViewById(R.id.email_input)
        val emailEditText: EditText = view.findViewById(R.id.email_input_edit_text)
        val sendEmailButton: Button = view.findViewById(R.id.reset_button)

        emailEditText.addTextChangedListener(RemoveErrorWhenEditTextChanges(emailInputLayout))

        fun setInputsEnabled(enabled: Boolean) {
            emailInputLayout.isEnabled = enabled
            sendEmailButton.isEnabled = enabled
        }

        val navigator: Navigator = DefaultNavigator.getInstance(this)

        val firebaseAuth = FirebaseAuth.getInstance()

        sendEmailButton.setOnClickListener {
            setInputsEnabled(false)
            val email = emailEditText.text.toString()

            val isValidEmail = AuthUtils.isValidEmail(email)
            if (!isValidEmail) {
                emailInputLayout.error = getString(R.string.invalid_email)
            }

            if (!isValidEmail) {
                setInputsEnabled(true)
                return@setOnClickListener
            }

            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this.requireActivity()) { result ->
                    if (result.isSuccessful) {
                        Snackbar.make(
                            view,
                            getString(R.string.reset_email_sent),
                            Snackbar.LENGTH_LONG
                        ).show()
                        navigator.navigateBack()
                    } else {
                        setInputsEnabled(true)
                        result.exception?.let {
                            Snackbar.make(
                                view,
                                it.localizedMessage ?: getString(R.string.failed_to_reset_pass),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }
    }
}
