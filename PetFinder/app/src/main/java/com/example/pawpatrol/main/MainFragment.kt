package com.example.pawpatrol.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R

class MainFragment : Fragment(R.layout.fragment_main) {

    companion object {
        const val TAG = "MainFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView: RecyclerView = view.findViewById(R.id.pets_list)
    }
}
