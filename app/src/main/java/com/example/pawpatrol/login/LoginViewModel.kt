package com.example.pawpatrol.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pawpatrol.user.UserService

class LoginViewModel(
    private val userService: UserService
) : ViewModel() {

    private val states = MutableLiveData<State>(State.Idle)

    private inline fun ifIdle(action: () -> Unit) {
        val currentState = states.value ?: State.Idle
        if (currentState === State.Idle || currentState is State.AuthorizationFailed) {
            action()
        }
    }

    fun attemptLogin(username: String, password: String) {
        ifIdle {
            states.value = State.Authorizing
            userService.login(username, password) { result ->
                val newState: State = when (result) {
                    UserService.LoginResult.Success ->
                        State.Authorized
                    is UserService.LoginResult.Failure ->
                        when (result.cause) {
                            UserService.LoginResult.Failure.Cause.UNKNOWN_USERNAME ->
                                State.AuthorizationFailed.InvalidUsername
                            UserService.LoginResult.Failure.Cause.INVALID_PASSWORD ->
                                State.AuthorizationFailed.InvalidPassword
                            UserService.LoginResult.Failure.Cause.OTHER ->
                                State.AuthorizationFailed.Other(result.msg)
                        }
                }
                states.postValue(newState)
            }
        }
    }

    sealed class State {
        object Idle : State()
        object Authorizing : State()
        object Authorized : State()
        sealed class AuthorizationFailed : State() {
            object InvalidUsername : AuthorizationFailed()
            object InvalidPassword : AuthorizationFailed()
            data class Other(val msg: String) : AuthorizationFailed()
        }
    }

    fun state(): LiveData<State> {
        return states
    }
}
