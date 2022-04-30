package com.example.pawpatrol.user

interface UserService {

    fun isLoggedIn(callback: (Boolean) -> Unit)

    fun login(username: String, password: String, callback: (LoginResult) -> Unit)

    fun fetchProfileData(callback: (FetchProfileResult) -> Unit)

    fun logOut(callback: () -> Unit)

    sealed class LoginResult {

        object Success : LoginResult()

        data class Failure(
            val cause: Cause,
            val msg: String
        ) : LoginResult() {

            enum class Cause {
                UNKNOWN_USERNAME,
                INVALID_PASSWORD,
                OTHER,
            }
        }
    }

    sealed class FetchProfileResult {

        data class Success(
            val id: String,
            val username: String,
            val email: String,
            val phoneNumber: String?,
        ) : FetchProfileResult()

        data class Failure(
            val cause: Cause,
            val msg: String
        ) : FetchProfileResult() {

            enum class Cause {
                UNAUTHORIZED,
                OTHER,
            }
        }
    }

    object NOOP : UserService {

        override fun isLoggedIn(callback: (Boolean) -> Unit) {
        }

        override fun login(username: String, password: String, callback: (LoginResult) -> Unit) {
        }

        override fun fetchProfileData(callback: (FetchProfileResult) -> Unit) {
        }

        override fun logOut(callback: () -> Unit) {
        }
    }
}

fun (() -> UserService).lazyDelegate(): UserService {
    val wrapped = lazy(this)
    return object : UserService {

        override fun isLoggedIn(callback: (Boolean) -> Unit) {
            wrapped.value.isLoggedIn(callback)
        }

        override fun login(
            username: String,
            password: String,
            callback: (UserService.LoginResult) -> Unit
        ) {
            wrapped.value.login(username, password, callback)
        }

        override fun fetchProfileData(callback: (UserService.FetchProfileResult) -> Unit) {
            wrapped.value.fetchProfileData(callback)
        }

        override fun logOut(callback: () -> Unit) {
            wrapped.value.logOut(callback)
        }
    }
}
