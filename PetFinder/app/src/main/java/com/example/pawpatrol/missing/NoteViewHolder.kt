package com.example.pawpatrol.missing

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.pawpatrol.R
import com.example.pawpatrol.util.ImageSource
import com.google.firebase.storage.StorageReference
import timber.log.Timber

class NoteViewHolder(
    itemView: View,
    private val glide: RequestManager
) : RecyclerView.ViewHolder(itemView) {

    private val photoImageView: ImageView = itemView.findViewById(R.id.main_attached_photo)
    private val nameView: TextView = itemView.findViewById(R.id.pet_name)
    private val descriptionView: TextView = itemView.findViewById(R.id.short_description)

    fun bind(missingPetNote: MissingPetNote) {
        nameView.text = missingPetNote.petName
        descriptionView.text = missingPetNote.description

        val imageUuid = missingPetNote.imageUuid
        if (imageUuid != null) {
            glide.load(ImageSource.Firebase(imageUuid).toTarget())
                .addListener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (e != null) {
                            Timber.w(e, "Failed to load image with uuid: %s", imageUuid)
                        } else {
                            Timber.w("Failed to load image with uuid: %s", imageUuid)
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Timber.d("Loaded image with uuid: %s successfully", imageUuid)
                        return false
                    }
                })
                .into(photoImageView)
        } else {
            glide.clear(photoImageView)
            photoImageView.setImageDrawable(null)
        }
    }

    fun unbind() {
        glide.clear(photoImageView)
    }
}
