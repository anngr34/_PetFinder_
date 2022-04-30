package com.example.pawpatrol.login

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pawpatrol.R
import com.example.pawpatrol.navigation.Navigator
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var navigator: Navigator

    private lateinit var viewModelProvider: ViewModelProvider

    companion object {
        const val TAG = "LoginFragment"
    }

    fun inject(
        navigator: Navigator,
        viewModelFactory: ViewModelProvider.Factory
    ) {
        this.navigator = navigator
        viewModelProvider = ViewModelProvider(this, viewModelFactory)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val usernameInputLayout: TextInputLayout = view.findViewById(R.id.username_input)
        val usernameEditText: EditText = view.findViewById(R.id.username_input_edit_text)
        val passwordInputLayout: TextInputLayout = view.findViewById(R.id.password_input)
        val passwordEditText: EditText = view.findViewById(R.id.password_input_edit_text)
        val restorePasswordButton: Button = view.findViewById(R.id.restore_password_button)
        val loginButton: Button = view.findViewById(R.id.login_button)
        val registerButton: Button = view.findViewById(R.id.register_button)

        fun setInputsEnabled(enabled: Boolean) {
            usernameInputLayout.isEnabled = enabled
            passwordInputLayout.isEnabled = enabled
            restorePasswordButton.isEnabled = enabled
            loginButton.isEnabled = enabled
            registerButton.isEnabled = enabled
        }

        val viewModel = viewModelProvider.get(LoginViewModel::class.java)

        viewModel.state().observe(viewLifecycleOwner) { state ->
            when (state) {
                LoginViewModel.State.Idle -> {
                    setInputsEnabled(true)
                }
                LoginViewModel.State.Authorizing -> {
                    setInputsEnabled(false)
                }
                LoginViewModel.State.Authorized -> {
                    navigator.navigateToMainApp()
                }
                LoginViewModel.State.AuthorizationFailed.InvalidUsername -> {
                    setInputsEnabled(true)
                    usernameInputLayout.error = getString(R.string.invalid_username)
                }
                LoginViewModel.State.AuthorizationFailed.InvalidPassword -> {
                    setInputsEnabled(true)
                    usernameInputLayout.error = getString(R.string.invalid_password)
                }
                is LoginViewModel.State.AuthorizationFailed.Other -> {
                    setInputsEnabled(true)
                    Snackbar.make(view, state.msg, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        restorePasswordButton.setOnClickListener {
            navigator.navigateToResetPassword()
        }
        loginButton.setOnClickListener {
            viewModel.attemptLogin(
                username = usernameEditText.text.toString(),
                password = passwordEditText.text.toString()
            )
        }
        registerButton.setOnClickListener {
            navigator.navigateToCreateAccount()
        }
    }
}
