package com.example.pawpatrol.notedetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.pawpatrol.R
import com.example.pawpatrol.util.ImageSource

class AttachedPhotosAdapter(
    private val glide: RequestManager,
    private val showDetach: Boolean,
    private val onDeletePhoto: (ImageSource, Int) -> Unit = { _, _ -> },
) : RecyclerView.Adapter<AttachedPhotoViewHolder>() {

    private var attachedImages: List<ImageSource> = emptyList()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachedPhotoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.attached_image_view_holder, parent, false)
        return AttachedPhotoViewHolder(itemView, glide)
    }

    override fun onBindViewHolder(holder: AttachedPhotoViewHolder, position: Int) {
        val imageSource = attachedImages[position]
        holder.bind(imageSource = imageSource, showDetach = showDetach) {
            onDeletePhoto(imageSource, position)
        }
    }

    override fun onViewRecycled(holder: AttachedPhotoViewHolder) {
        holder.unbind()
    }

    override fun getItemCount(): Int {
        return attachedImages.size
    }

    override fun getItemId(position: Int): Long {
        return attachedImages[position].hashCode().toLong()
    }

    fun updateImages(attachedImages: List<ImageSource>) {
        this.attachedImages = attachedImages.toList()
        notifyDataSetChanged()
    }
}