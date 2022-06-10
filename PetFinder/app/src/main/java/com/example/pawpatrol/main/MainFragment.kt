package com.example.pawpatrol.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.pawpatrol.R
import com.example.pawpatrol.navigation.DefaultNavigator
import com.google.android.material.appbar.AppBarLayout

class MainFragment : Fragment(R.layout.fragment_main) {

    companion object {
        const val TAG = "MainFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val navigator = DefaultNavigator.getInstance(this)

            navigator.navigateToSearch()
        }

        val topBar: AppBarLayout = view.findViewById(R.id.top_bar)
        val toolbar: Toolbar = topBar.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
    }
}
