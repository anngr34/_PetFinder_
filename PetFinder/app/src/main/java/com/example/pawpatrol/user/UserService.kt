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
}
