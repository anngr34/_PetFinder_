package com.example.pawpatrol.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.pawpatrol.R
import com.example.pawpatrol.login.LoginFragment
import com.example.pawpatrol.login.RegisterFragment
import com.example.pawpatrol.login.ResetPasswordFragment
import com.example.pawpatrol.main.MainFragment
import com.example.pawpatrol.missing.MissingPetNoteCreationFragment
import com.example.pawpatrol.profile.ProfileFragment
import com.example.pawpatrol.missing.SearchAnimalsFragment
import com.example.pawpatrol.notedetails.NoteDetailsFragment
import timber.log.Timber

class DefaultNavigator(
    @IdRes private val contentRootId: Int,
    private val fragmentManager: FragmentManager,
) : Navigator {

    private val hostFragment: Fragment?
        get() = fragmentManager.findFragmentByTag(MainFragment.TAG)

    override fun navigateToMainApp() {
        Timber.d("navigateToMainApp")
        fragmentManager
            .beginTransaction()
            .replace(contentRootId, MainFragment(), MainFragment.TAG)
            .commitAllowingStateLoss()
    }

    override fun navigateToLogin() {
        Timber.d("navigateToLogin")
        fragmentManager
            .beginTransaction()
            .replace(contentRootId, LoginFragment(), LoginFragment.TAG)
            .commitAllowingStateLoss()
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
        fragmentManager
            .beginTransaction()
            .replace(contentRootId, ResetPasswordFragment(), ResetPasswordFragment.TAG)
            .addToBackStack(ResetPasswordFragment.TAG)
            .commit()
    }

    override fun navigateBack(): Boolean {
        Timber.d("navigateBack")
        val internalPagePopped = internalPop()
        return if (!internalPagePopped) {
            externalPop()
        } else {
            true
        }
    }

    private fun externalPop(): Boolean {
        Timber.d("externalPop")
        return if (fragmentManager.backStackEntryCount >= 1) {
            Timber.d("externalPop scheduled")
            fragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    private fun internalPop(): Boolean {
        Timber.d("internalPop")
        val mainFragment = hostFragment
        return if (mainFragment != null) {
            val childFm = mainFragment.childFragmentManager
            if (childFm.backStackEntryCount > 1) {
                Timber.d("internalPop scheduled")
                childFm.popBackStack()
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    override fun navigateToSearch() {
        Timber.d("navigateToSearch")
        hostFragment?.run {
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.under_toolbar_content,
                    SearchAnimalsFragment(),
                    SearchAnimalsFragment.TAG
                )
                .addToBackStack(SearchAnimalsFragment.TAG)
                .commit()
        }
    }

    override fun navigateToProfile() {
        Timber.d("navigateToProfile")
        hostFragment?.run {
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.under_toolbar_content,
                    ProfileFragment(),
                    ProfileFragment.TAG
                )
                .addToBackStack(ProfileFragment.TAG)
                .commit()
        }
    }

    override fun navigateToNoteCreation() {
        Timber.d("navigateToNoteCreation")
        hostFragment?.run {
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.under_toolbar_content,
                    MissingPetNoteCreationFragment(),
                    MissingPetNoteCreationFragment.TAG
                )
                .addToBackStack(MissingPetNoteCreationFragment.TAG)
                .commit()
        }
    }

    override fun navigateToNoteDetails(authorId: String, noteId: String) {
        Timber.d("navigateToNoteDetails")
        hostFragment?.run {
            val fragment = NoteDetailsFragment.newInstance(authorId, noteId)
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.under_toolbar_content,
                    fragment,
                    NoteDetailsFragment.TAG
                )
                .addToBackStack(NoteDetailsFragment.TAG)
                .commit()
        }
    }

    override fun navigateToReportCreation(noteId: String) {
        Timber.d("navigateToReportCreation")
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
