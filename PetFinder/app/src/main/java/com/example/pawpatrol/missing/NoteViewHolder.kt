package com.example.pawpatrol.missing

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R

class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val photoImageView: ImageView = itemView.findViewById(R.id.main_attached_photo)
    private val nameView: TextView = itemView.findViewById(R.id.pet_name)
    private val descriptionView: TextView = itemView.findViewById(R.id.short_description)

    fun bind(missingPetNote: MissingPetNote) {
        nameView.text = missingPetNote.petName
        descriptionView.text = missingPetNote.description
    }
}
