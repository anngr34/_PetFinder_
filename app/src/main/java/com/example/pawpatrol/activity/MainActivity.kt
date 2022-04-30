package com.example.pawpatrol.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.pawpatrol.R
import com.example.pawpatrol.navigation.Navigator
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var navigator: Navigator

    private lateinit var viewModelProvider: ViewModelProvider

    fun inject(navigator: Navigator, viewModelFactory: ViewModelProvider.Factory) {
        Timber.d("inject, navigator: $navigator, viewModelFactory: $viewModelFactory")
        this.navigator = navigator
        viewModelProvider = ViewModelProvider(this, viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("onCreate")

        setContentView(R.layout.activity_main)

        val viewModel = viewModelProvider.get(AuthViewModel::class.java)

        viewModel.state().observe(this) { state ->
            Timber.d("onCreate, fetched state: $state")
            when (state) {
                AuthViewModel.State.IDLE -> {
                    viewModel.fetchCurrentStatus()
                }
                AuthViewModel.State.FETCHING -> {
                    // wait
                }
                AuthViewModel.State.AUTHORIZED -> {
                    navigator.navigateToMainApp()
                }
                AuthViewModel.State.UNAUTHORIZED -> {
                    navigator.navigateToLogin()
                }
            }
        }
    }
}
