package com.example.pawpatrol.navigation

interface Navigator {

    fun navigateToMainApp()

    fun navigateToLogin()

    fun navigateToCreateAccount()

    fun navigateToResetPassword()

    fun navigateBack(): Boolean

    fun navigateToSearch()

    fun navigateToProfile()

    fun navigateToNoteCreation()

    fun navigateToNoteDetails(authorId: String, noteId: String)

    fun navigateToReportCreation(noteId: String)
}
