package com.example.pawpatrol.navigation

import com.example.pawpatrol.missing.MissingPetNote

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

    fun navigateToNoteEdit(note: MissingPetNote)

    fun navigateToReports(noteId: String)
}
