package com.example.pawpatrol.notedetails

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.pawpatrol.R
import com.example.pawpatrol.util.ImageSource

class AttachedPhotoViewHolder(
    itemView: View,
    private val glide: RequestManager,
) : RecyclerView.ViewHolder(itemView) {

    private val attachedPhotoImageView: ImageView =
        itemView.findViewById(R.id.attached_photo_image_view)

    private val detachPhotoButton: View = itemView.findViewById(R.id.detach_photo_button)

    fun bind(
        imageSource: ImageSource,
        showDetach: Boolean = true,
        detachClickListener: View.OnClickListener? = null
    ) {
        glide.load(imageSource.toTarget())
            .into(attachedPhotoImageView)

        detachPhotoButton.visibility = if (showDetach) {
            View.VISIBLE
        } else {
            View.GONE
        }

        detachPhotoButton.setOnClickListener(detachClickListener)
    }

    fun unbind() {
        glide.clear(attachedPhotoImageView)
        detachPhotoButton.setOnClickListener(null)
    }
}