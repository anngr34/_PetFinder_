package com.example.pawpatrol.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import com.example.pawpatrol.login.LoginFragment
import com.example.pawpatrol.main.MainFragment
import timber.log.Timber

class DefaultNavigator(
    @IdRes private val contentRootId: Int,
    private val fragmentManager: FragmentManager,
) : Navigator {

    override fun navigateToMainApp() {
        Timber.d("navigateToMainApp")
        fragmentManager
            .beginTransaction()
            .replace(contentRootId, MainFragment(), MainFragment.TAG)
            .commit()
    }

    override fun navigateToLogin() {
        Timber.d("navigateToLogin")
        fragmentManager
            .beginTransaction()
            .replace(contentRootId, LoginFragment(), LoginFragment.TAG)
            .commit()
    }

    override fun navigateToCreateAccount() {
        Timber.d("navigateToCreateAccount")
    }

    override fun navigateToResetPassword() {
        Timber.d("navigateToResetPassword")
    }
}