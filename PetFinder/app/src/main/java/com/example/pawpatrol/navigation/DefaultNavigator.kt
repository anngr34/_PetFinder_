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
import com.example.pawpatrol.missing.MissingPetNote
import com.example.pawpatrol.missing.MissingPetNoteCreationFragment
import com.example.pawpatrol.missing.SearchAnimalsFragment
import com.example.pawpatrol.notedetails.CreateReportFragment
import com.example.pawpatrol.notedetails.NoteDetailsFragment
import com.example.pawpatrol.notedetails.ReportsToNoteFragment
import com.example.pawpatrol.profile.ProfileFragment
import timber.log.Timber

class DefaultNavigator(
    @IdRes private val contentRootId: Int,
    private val fragmentManager: FragmentManager,
) : Navigator {

    private val hostFragment: Fragment?
        get() = fragmentManager.findFragmentByTag(MainFragment.TAG)

    override fun navigateToMainApp() {
        Timber.d("try navigateToMainApp")
        if (hostFragment != null) {
            Timber.d("already in main part, ignore")
            return
        }
        if (fragmentManager.backStackEntryCount > 0) {
            Timber.d("pop login flow")
            fragmentManager.popBackStack(
                LoginFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        Timber.d("schedule navigateToMainApp")
        fragmentManager
            .beginTransaction()
            .add(contentRootId, MainFragment(), MainFragment.TAG)
            .addToBackStack(MainFragment.TAG)
            .commitAllowingStateLoss()
    }

    override fun navigateToLogin() {
        Timber.d("try navigateToLogin")
        if (fragmentManager.backStackEntryCount > 0 && fragmentManager.getBackStackEntryAt(0).name == LoginFragment.TAG) {
            return
        }
        if (fragmentManager.backStackEntryCount > 0) {
            Timber.d("pop main flow")
            fragmentManager.popBackStack(MainFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        Timber.d("schedule navigateToLogin")
        fragmentManager
            .beginTransaction()
            .add(contentRootId, LoginFragment(), LoginFragment.TAG)
            .addToBackStack(LoginFragment.TAG)
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
        val mainFlowPopped = mainFlowPop()
        return if (!mainFlowPopped) {
            externalPop()
        } else {
            true
        }
    }

    private fun mainFlowPop(): Boolean {
        Timber.d("try mainFlowPop")
        val mainFragment = hostFragment ?: return false

        val childFm = mainFragment.childFragmentManager
        return if (childFm.backStackEntryCount > 1) {
            Timber.d("schedule mainFlowPop")
            childFm.popBackStack()
            true
        } else {
            Timber.d("nothing to pop in main flow")
            false
        }
    }

    private fun externalPop(): Boolean {
        Timber.d("try externalPop")
        return if (fragmentManager.backStackEntryCount > 1) {
            Timber.d("schedule externalPop")
            fragmentManager.popBackStack()
            true
        } else {
            Timber.d("nothing to pop externally")
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
                    MissingPetNoteCreationFragment.newInstance(),
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
        hostFragment?.run {
            val fragment = CreateReportFragment.newInstance(noteId)
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.under_toolbar_content,
                    fragment,
                    CreateReportFragment.TAG
                )
                .addToBackStack(CreateReportFragment.TAG)
                .commit()
        }
    }

    override fun navigateToReports(noteId: String) {
        Timber.d("navigateToReports")
        hostFragment?.run {
            val fragment = ReportsToNoteFragment.newInstance(noteId)
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.under_toolbar_content,
                    fragment,
                    ReportsToNoteFragment.TAG
                )
                .addToBackStack(ReportsToNoteFragment.TAG)
                .commit()
        }
    }

    override fun navigateToNoteEdit(note: MissingPetNote) {
        Timber.d("navigateToNoteEdit")
        hostFragment?.run {
            val fragment = MissingPetNoteCreationFragment.newInstance(note)
            childFragmentManager
                .beginTransaction()
                .replace(
                    R.id.under_toolbar_content,
                    fragment,
                    MissingPetNoteCreationFragment.TAG
                )
                .addToBackStack(MissingPetNoteCreationFragment.TAG)
                .commit()
        }
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
