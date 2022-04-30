package com.example.pawpatrol.app

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.pawpatrol.login.LoginFragment
import com.example.pawpatrol.login.LoginScreenViewModelFactory
import com.example.pawpatrol.activity.MainActivityComponent
import com.example.pawpatrol.common.Attachable
import timber.log.Timber
import java.io.Closeable

class AttachMainActivityComponent(
    private val mainActivityComponent: MainActivityComponent,
    private val fragmentManager: FragmentManager
) : Attachable {

    override fun attach(): Closeable {
        Timber.d("Attach: $this, mainActivityComponent: $mainActivityComponent, $fragmentManager")

        val callbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentPreAttached(
                fragmentManager: FragmentManager,
                fragment: Fragment,
                context: Context
            ) {
                if (fragment is LoginFragment) {
                    injectLoginFragment(fragment)
                }
            }
        }

        Timber.d("Register fragment manager callbacks: $callbacks")
        fragmentManager.registerFragmentLifecycleCallbacks(
            callbacks,
            true
        )

        return Closeable {
            Timber.d("Unregister fragment manager callbacks: $callbacks")
            fragmentManager.unregisterFragmentLifecycleCallbacks(callbacks)
        }
    }

    private fun injectLoginFragment(fragment: LoginFragment) {
        fragment.inject(
            navigator = mainActivityComponent.navigator,
            viewModelFactory = LoginScreenViewModelFactory(mainActivityComponent.userService)
        )
    }
}