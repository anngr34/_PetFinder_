package com.example.pawpatrol.missing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpatrol.R

class NotesAdapter(
    private val navigateToNoteDetails: (String, String) -> Unit
) : RecyclerView.Adapter<NoteViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private var items: List<MissingPetNote> = emptyList()

    fun update(newItems: List<MissingPetNote>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.missing_pet_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = items[position]
        holder.bind(note)
        holder.itemView.setOnClickListener {
            navigateToNoteDetails(note.noteCreatorUid, note.noteUid)
        }
    }

    override fun onViewRecycled(holder: NoteViewHolder) {
        holder.itemView.setOnClickListener(null)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].noteUid.hashCode().toLong()
    }
}
