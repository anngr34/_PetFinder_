package com.example.pawpatrol.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pawpatrol.user.UserService
import java.lang.IllegalArgumentException

class LoginScreenViewModelFactory(
    private val userService: UserService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == LoginViewModel::class.java) {
            return LoginViewModel(userService) as T
        }
        throw IllegalArgumentException("Cannot create ViewModel of type: $modelClass!")
    }
}
