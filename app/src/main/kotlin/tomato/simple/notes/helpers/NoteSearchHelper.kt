package tomato.simple.notes.helpers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tomato.simple.notes.R
import tomato.simple.notes.extensions.notesDB
import tomato.simple.notes.extensions.notebooksDB
import tomato.simple.notes.models.ChecklistItem
import tomato.simple.notes.models.CounterItem
import tomato.simple.notes.models.Note
import tomato.simple.notes.models.NoteType
import tomato.simple.notes.models.Notebook

data class NoteSearchResult(
    val notebook: Notebook? = null,
    val note: Note? = null,
    val title: String,
    val subtitle: String
) {
    val isNotebook get() = notebook != null
}

class NoteSearchHelper(private val context: Context) {
    fun search(query: String): List<NoteSearchResult> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) {
            return emptyList()
        }

        val results = mutableListOf<NoteSearchResult>()
        val notebooks = context.notebooksDB.getNotebooks()
        val notebooksById = notebooks.associateBy { it.id }

        notebooks.filter { it.title.lowercase().contains(needle) }.forEach { notebook ->
            results.add(
                NoteSearchResult(
                    notebook = notebook,
                    title = notebook.title,
                    subtitle = context.getString(R.string.notebooks)
                )
            )
        }

        context.notesDB.getNotes().forEach { note ->
            val notebook = notebooksById[note.notebookId]
            if (notebook?.isLocked() == true && !note.title.lowercase().contains(needle)) {
                return@forEach
            }

            val searchableText = buildSearchableText(note, includeBody = notebook?.isLocked() != true && !note.isLocked())
            if (searchableText.contains(needle) || note.title.lowercase().contains(needle)) {
                results.add(
                    NoteSearchResult(
                        note = note,
                        title = note.title,
                        subtitle = notebook?.title ?: context.getString(R.string.notebooks)
                    )
                )
            }
        }

        return results
    }

    private fun buildSearchableText(note: Note, includeBody: Boolean): String {
        val builder = StringBuilder(note.title.lowercase())
        if (!includeBody) {
            return builder.toString()
        }

        val stored = note.getNoteStoredValue(context) ?: note.value
        when (note.type) {
            NoteType.TYPE_TEXT -> builder.append(' ').append(stored.lowercase())
            NoteType.TYPE_CHECKLIST -> {
                try {
                    val type = object : TypeToken<List<ChecklistItem>>() {}.type
                    val items = Gson().fromJson<List<ChecklistItem>>(stored.ifEmpty { "[]" }, type) ?: emptyList()
                    items.forEach { builder.append(' ').append(it.title.lowercase()) }
                } catch (_: Exception) {
                    builder.append(' ').append(stored.lowercase())
                }
            }
            NoteType.TYPE_COUNTER -> {
                try {
                    val type = object : TypeToken<List<CounterItem>>() {}.type
                    val items = Gson().fromJson<List<CounterItem>>(stored.ifEmpty { "[]" }, type) ?: emptyList()
                    items.forEach { builder.append(' ').append(it.title.lowercase()) }
                } catch (_: Exception) {
                    builder.append(' ').append(stored.lowercase())
                }
            }
        }
        return builder.toString()
    }
}
