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

class LoginFragment : Fragment(R.layout.fragment_login) {

    companion object {
        const val TAG = "LoginFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val emailInputLayout: TextInputLayout = view.findViewById(R.id.email_input)
        val emailEditText: EditText = view.findViewById(R.id.email_input_edit_text)
        val passwordInputLayout: TextInputLayout = view.findViewById(R.id.password_input)
        val passwordEditText: EditText = view.findViewById(R.id.password_input_edit_text)
        val restorePasswordButton: Button = view.findViewById(R.id.restore_password_button)
        val loginButton: Button = view.findViewById(R.id.login_button)
        val registerButton: Button = view.findViewById(R.id.register_button)

        emailEditText.addTextChangedListener(RemoveErrorWhenEditTextChanges(emailInputLayout))
        passwordEditText.addTextChangedListener(RemoveErrorWhenEditTextChanges(passwordInputLayout))

        fun setInputsEnabled(enabled: Boolean) {
            emailInputLayout.isEnabled = enabled
            passwordInputLayout.isEnabled = enabled
            restorePasswordButton.isEnabled = enabled
            loginButton.isEnabled = enabled
            registerButton.isEnabled = enabled
        }

        val navigator: Navigator = DefaultNavigator.getInstance(this)

        val firebaseAuth = FirebaseAuth.getInstance()

        restorePasswordButton.setOnClickListener {
            navigator.navigateToResetPassword()
        }
        loginButton.setOnClickListener {
            setInputsEnabled(false)
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            val isValidEmail = AuthUtils.isValidEmail(email)
            if (!isValidEmail) {
                emailInputLayout.error = getString(R.string.invalid_email)
            }
            val isValidPassword = AuthUtils.isValidPassword(password)
            if (!isValidPassword) {
                passwordInputLayout.error = getString(R.string.invalid_password)
            }

            if (!isValidEmail || !isValidPassword) {
                setInputsEnabled(true)
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this.requireActivity()) { result ->
                    if (result.isSuccessful) {
                        navigator.navigateToMainApp()
                    } else {
                        setInputsEnabled(true)
                        result.exception?.let {
                            Snackbar.make(
                                view,
                                it.localizedMessage ?: getString(R.string.failed_to_authenticate),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }
        registerButton.setOnClickListener {
            navigator.navigateToCreateAccount()
        }
    }
}
