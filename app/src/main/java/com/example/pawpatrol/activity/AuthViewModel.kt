package com.example.pawpatrol.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawpatrol.user.UserService

class AuthViewModel(
    private val userService: UserService
) : ViewModel() {

    private val states = MutableLiveData(State.IDLE)

    enum class State {
        IDLE,
        FETCHING,
        AUTHORIZED,
        UNAUTHORIZED,
    }

    private inline fun ifNotFetching(callback: () -> Unit) {
        val currentState = states.value ?: State.IDLE
        if (currentState != State.FETCHING) {
            callback()
        }
    }

    fun fetchCurrentStatus() {
        ifNotFetching {
            states.value = State.FETCHING
            userService.isLoggedIn { isLoggedIn ->
                val newState = if (isLoggedIn) {
                    State.AUTHORIZED
                } else {
                    State.UNAUTHORIZED
                }
                states.postValue(newState)
            }
        }
    }

    fun state(): LiveData<State> {
        return states
    }
}
