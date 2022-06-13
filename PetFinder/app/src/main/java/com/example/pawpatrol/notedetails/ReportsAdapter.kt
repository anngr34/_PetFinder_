package com.example.pawpatrol.notedetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.pawpatrol.R
import com.google.firebase.storage.StorageReference

class ReportsAdapter(
    private val glide: RequestManager
) : RecyclerView.Adapter<ReportItemViewHolder>() {

    private var reports: List<PetReport> = emptyList()

    private val sharedViewHolderPool = RecyclerView.RecycledViewPool()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.report_item, parent, false)
        return ReportItemViewHolder(itemView, glide, sharedViewHolderPool)
    }

    override fun onBindViewHolder(holder: ReportItemViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    override fun onViewRecycled(holder: ReportItemViewHolder) {
        holder.unbind()
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    override fun getItemId(position: Int): Long {
        return reports[position].reportId.hashCode().toLong()
    }

    fun update(reports: List<PetReport>) {
        this.reports = reports.toList()
        notifyDataSetChanged()
    }
}
