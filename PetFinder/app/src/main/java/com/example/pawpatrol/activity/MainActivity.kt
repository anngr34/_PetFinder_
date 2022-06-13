package com.example.pawpatrol.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pawpatrol.R
import com.example.pawpatrol.navigation.DefaultNavigator
import com.example.pawpatrol.navigation.Navigator
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val isLoggedIn = firebaseAuth.currentUser != null
        Timber.d("Auth state changed, logged in: $isLoggedIn")
        val navigator: Navigator = DefaultNavigator.getInstance(this)
        if (isLoggedIn) {
            navigator.navigateToMainApp()
        } else {
            navigator.navigateToLogin()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()

        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }

    override fun onBackPressed() {
        val handled = DefaultNavigator.getInstance(this).navigateBack()
        if (!handled) {
            finish()
        }
    }
}
