package com.example.pawpatrol.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pawpatrol.user.UserService

class MainActivityViewModelFactory(
    private val userService: UserService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == AuthViewModel::class.java) {
            return AuthViewModel(userService) as T
        }
        throw IllegalArgumentException("Cannot create ViewModel of type: $modelClass!")
    }
}
