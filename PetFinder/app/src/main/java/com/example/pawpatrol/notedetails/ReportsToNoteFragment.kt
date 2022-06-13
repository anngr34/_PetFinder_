package com.example.pawpatrol.notedetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R
import com.example.pawpatrol.app.PawApplication
import com.example.pawpatrol.data.FirebaseHolder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

private const val ARG_NOTE_ID = "note_id"

class ReportsToNoteFragment : Fragment(R.layout.fragment_reports) {

    companion object {

        const val TAG = "ReportsToNoteFragment"

        fun newInstance(noteId: String): ReportsToNoteFragment {
            return ReportsToNoteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTE_ID, noteId)
                }
            }
        }
    }

    private val performOnPause: MutableList<() -> Unit> = mutableListOf()

    private val reportsAdapter = ReportsAdapter(
        glide = PawApplication.instance.glide,
    )

    private lateinit var progressBar: View
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var stubView: View

    private val dataListener = object : ValueEventListener {

        override fun onDataChange(reportsSnapshot: DataSnapshot) {
            val reports = reportsSnapshot.children.mapNotNull { reportSnapshot ->
                val reportId = reportSnapshot.key ?: return@mapNotNull null
                val json = reportSnapshot.value as? Map<String, Any?> ?: return@mapNotNull null
                PetReport.parse(reportId, json)
            }
            Timber.d("Reports: %s", reports)

            progressBar.visibility = View.GONE
            if (reports.isNotEmpty()) {
                reportsRecyclerView.visibility = View.VISIBLE
                stubView.visibility = View.GONE
            } else {
                reportsRecyclerView.visibility = View.GONE
                stubView.visibility = View.VISIBLE
            }
            reportsAdapter.update(reports)
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.w("Failure while getting reports", error)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressBar = view.findViewById(R.id.progress_bar)
        reportsRecyclerView = view.findViewById(R.id.reports_recycler_view)
        stubView = view.findViewById(R.id.stub_view)

        reportsRecyclerView.apply {
            setHasFixedSize(true)
            adapter = reportsAdapter
        }
    }

    override fun onResume() {
        super.onResume()

        val noteId = requireArguments().getString(ARG_NOTE_ID)!!
        val dbRef = FirebaseHolder.reportsForNote(noteId)
        dbRef.addListenerForSingleValueEvent(dataListener)
        performOnPause.add {
            dbRef.removeEventListener(dataListener)
        }
    }

    override fun onPause() {
        super.onPause()

        performOnPause.forEach {
            it.invoke()
        }
        performOnPause.clear()
    }
}
