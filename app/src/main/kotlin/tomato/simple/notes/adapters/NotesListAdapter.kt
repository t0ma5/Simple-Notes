package tomato.simple.notes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.commons.extensions.getProperTextColor
import tomato.simple.notes.R
import tomato.simple.notes.databinding.ItemNoteCardBinding
import tomato.simple.notes.helpers.previewText
import tomato.simple.notes.models.Note
import tomato.simple.notes.models.Notebook

class NotesListAdapter(
    private var notes: List<Note>,
    private var notebooksById: Map<Long, Notebook>,
    private var showNotebookName: Boolean,
    private val itemClick: (Note) -> Unit,
) : RecyclerView.Adapter<NotesListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemNoteCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        val context = holder.itemView.context
        val primary = context.getProperPrimaryColor()
        val text = context.getProperTextColor()
        holder.binding.apply {
            noteCardTitle.text = note.title
            noteCardTitle.setTextColor(text)
            val preview = note.previewText(context)
            noteCardPreview.text = preview
            noteCardPreview.setTextColor(text)
            noteCardPreview.beGoneIf(preview.isNullOrBlank() || note.isLocked())
            noteCardLock.beVisibleIf(note.isLocked())
            if (note.isLocked()) {
                noteCardLock.setImageDrawable(
                    context.resources.getColoredDrawableWithColor(com.simplemobiletools.commons.R.drawable.ic_lock_vector, primary)
                )
            }
            noteCardPin.beVisibleIf(note.isPinned())
            if (note.isPinned()) {
                noteCardPin.setImageDrawable(context.resources.getColoredDrawableWithColor(R.drawable.ic_pin_vector, primary))
            }
            val notebookTitle = notebooksById[note.notebookId]?.title.orEmpty()
            noteCardNotebook.text = notebookTitle
            noteCardNotebook.setTextColor(text)
            noteCardNotebook.beVisibleIf(showNotebookName && notebookTitle.isNotEmpty())
            root.setOnClickListener { itemClick(note) }
        }
    }

    override fun getItemCount() = notes.size

    fun updateItems(newNotes: List<Note>, newNotebooks: Map<Long, Notebook>, showNotebook: Boolean) {
        notes = newNotes
        notebooksById = newNotebooks
        showNotebookName = showNotebook
        notifyDataSetChanged()
    }
}
