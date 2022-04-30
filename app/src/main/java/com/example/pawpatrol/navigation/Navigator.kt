package com.example.pawpatrol.navigation

interface Navigator {

    fun navigateToMainApp()

    fun navigateToLogin()

    fun navigateToCreateAccount()

    fun navigateToResetPassword()

    object NOOP : Navigator {

        override fun navigateToMainApp() {
        }

        override fun navigateToLogin() {
        }

        override fun navigateToCreateAccount() {
        }

        override fun navigateToResetPassword() {
        }
    }
}

fun (() -> Navigator).lazyDelegate(): Navigator {
    val wrapped = lazy(this)
    return object : Navigator {

        override fun navigateToMainApp() {
            wrapped.value.navigateToMainApp()
        }

        override fun navigateToLogin() {
            wrapped.value.navigateToLogin()
        }

        override fun navigateToCreateAccount() {
            wrapped.value.navigateToCreateAccount()
        }

        override fun navigateToResetPassword() {
            wrapped.value.navigateToResetPassword()
        }
    }
}
