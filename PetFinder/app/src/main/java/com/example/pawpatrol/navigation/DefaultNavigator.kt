package com.example.pawpatrol.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.pawpatrol.R
import com.example.pawpatrol.login.LoginFragment
import com.example.pawpatrol.main.MainFragment
import com.example.pawpatrol.register.RegisterFragment
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
        fragmentManager
            .beginTransaction()
            .replace(contentRootId, RegisterFragment(), RegisterFragment.TAG)
            .addToBackStack(RegisterFragment.TAG)
            .commit()
    }

    override fun navigateToResetPassword() {
        Timber.d("navigateToResetPassword")
    }

    companion object {

        fun getInstance(activity: FragmentActivity): DefaultNavigator {
            return DefaultNavigator(
                contentRootId = R.id.content_root,
                fragmentManager = activity.supportFragmentManager
            )
        }

        fun getInstance(fragment: Fragment): DefaultNavigator {
            return getInstance(fragment.requireActivity())
        }
    }
}
