package com.example.pawpatrol.notedetails

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.pawpatrol.R
import com.example.pawpatrol.util.ImageSource

class ReportItemViewHolder(
    itemView: View,
    glide: RequestManager,
    sharedViewHolderPool: RecyclerView.RecycledViewPool
) : RecyclerView.ViewHolder(itemView) {

    private val photosAdapter = AttachedPhotosAdapter(
        glide = glide,
        showDetach = false,
    )

    private val descriptionTextView: TextView = itemView.findViewById(R.id.report_description)
    private val attachedPhotosRecyclerView: RecyclerView =
        itemView.findViewById(R.id.attached_photos_recycler_view)

    init {
        attachedPhotosRecyclerView.setRecycledViewPool(sharedViewHolderPool)
        attachedPhotosRecyclerView.adapter = photosAdapter
    }

    fun bind(report: PetReport) {
        descriptionTextView.text = report.description

        report.attachedImageIds.let { imageIds ->
            photosAdapter.updateImages(imageIds.map { ImageSource.Firebase(it) })
            attachedPhotosRecyclerView.visibility = if (imageIds.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    fun unbind() {
        attachedPhotosRecyclerView.adapter = null
    }
}
