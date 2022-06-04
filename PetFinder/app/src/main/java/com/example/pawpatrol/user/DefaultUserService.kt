package com.example.pawpatrol.user

import android.content.Context
import com.example.pawpatrol.mocks.TestUsers
import com.example.pawpatrol.user.UserService.FetchProfileResult
import com.example.pawpatrol.user.UserService.LoginResult
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference

private const val AUTH_TOKEN_KEY = "token"

class DefaultUserService(
    private val appContext: Context,
    private val executor: Executor,
) : UserService {

    private val sharedPreferences by lazy {
        appContext.getSharedPreferences("auth-token", Context.MODE_PRIVATE)
    }

    private val lastToken = AtomicReference<String?>(null)

    private fun getLastToken(): String? {
        val token = lastToken.get()
        return if (token == null) {
            val savedToken = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
            if (savedToken != null) {
                lastToken.compareAndSet(null, savedToken)
            }
            lastToken.get()
        } else {
            token
        }
    }

    private fun updateToken(token: String) {
        lastToken.compareAndSet(null, token)
        sharedPreferences.edit()
            .putString(AUTH_TOKEN_KEY, token)
            .apply()
    }

    private fun clearToken() {
        lastToken.set(null)
        sharedPreferences.edit()
            .remove(AUTH_TOKEN_KEY)
            .apply()
    }

    override fun isLoggedIn(callback: (Boolean) -> Unit) {
        executor.execute {
            Timber.d("isLoggedIn execution started")
            val token = getLastToken()
            callback(token != null)
        }
        Timber.d("isLoggedIn scheduled")
    }

    override fun login(
        username: String,
        password: String,
        callback: (LoginResult) -> Unit
    ) {
        executor.execute {
            Timber.d("login execution started")
            val result: LoginResult = when(username) {
                TestUsers.ALWAYS_AUTHORIZED_USER.username -> {
                    updateToken(TestUsers.ALWAYS_AUTHORIZED_USER.token)
                    LoginResult.Success
                }
                TestUsers.NOT_EXISTING_USER.username -> {
                    LoginResult.Failure(
                        cause = LoginResult.Failure.Cause.INVALID_PASSWORD,
                        msg = "Username does not exist"
                    )
                }
                TestUsers.USER_WITH_PASS.username -> {
                    if (password == TestUsers.USER_WITH_PASS.password) {
                        updateToken(TestUsers.USER_WITH_PASS.token)
                        LoginResult.Success
                    } else {
                        LoginResult.Failure(
                            cause = LoginResult.Failure.Cause.INVALID_PASSWORD,
                            msg = "Wrong password"
                        )
                    }
                }
                else -> LoginResult.Failure(
                    cause = LoginResult.Failure.Cause.OTHER,
                    msg = "Real login not implemented yet"
                )
            }
            callback(result)
        }
        Timber.d("login: $username, $password, scheduled")
    }

    override fun fetchProfileData(callback: (FetchProfileResult) -> Unit) {
        executor.execute {
            Timber.d("fetchProfileData")
            val result = when (getLastToken()) {
                null ->
                    FetchProfileResult.Failure(
                        cause = FetchProfileResult.Failure.Cause.UNAUTHORIZED,
                        msg = "No auth token"
                    )
                TestUsers.ALWAYS_AUTHORIZED_USER.token ->
                    FetchProfileResult.Success(
                        id = TestUsers.ALWAYS_AUTHORIZED_USER.id,
                        username = TestUsers.ALWAYS_AUTHORIZED_USER.username,
                        email = TestUsers.ALWAYS_AUTHORIZED_USER.email,
                        phoneNumber = TestUsers.ALWAYS_AUTHORIZED_USER.phoneNumber,
                    )
                TestUsers.USER_WITH_PASS.token ->
                    FetchProfileResult.Success(
                        id = TestUsers.USER_WITH_PASS.id,
                        username = TestUsers.USER_WITH_PASS.username,
                        email = TestUsers.USER_WITH_PASS.email,
                        phoneNumber = TestUsers.USER_WITH_PASS.phoneNumber,
                    )
                else ->
                    FetchProfileResult.Failure(
                        cause = FetchProfileResult.Failure.Cause.OTHER,
                        msg = "Real fetch not implemented yet"
                    )
            }
            callback(result)
        }
        Timber.d("fetchProfileData scheduled")
    }

    override fun logOut(callback: () -> Unit) {
        executor.execute {
            Timber.d("logout started")
            clearToken()
            callback()
        }
        Timber.d("logout scheduled")
    }
}
