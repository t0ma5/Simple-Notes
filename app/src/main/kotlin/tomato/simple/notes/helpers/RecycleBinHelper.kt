package tomato.simple.notes.helpers

import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import tomato.simple.notes.extensions.config
import tomato.simple.notes.extensions.notesDB
import tomato.simple.notes.extensions.notebooksDB
import tomato.simple.notes.extensions.widgetsDB
import tomato.simple.notes.models.Note
import tomato.simple.notes.models.Notebook

class RecycleBinHelper(private val context: android.content.Context) {
    fun deleteNote(note: Note, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            val noteId = note.id
            if (noteId != null) {
                if (context.config.useRecycleBin) {
                    context.notesDB.updateDeletedTs(noteId, System.currentTimeMillis())
                } else {
                    context.notesDB.deleteNote(note)
                }
                context.widgetsDB.deleteNoteWidgets(noteId)
            }
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun deleteNotebook(notebook: Notebook, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            val notebookId = notebook.id ?: return@ensureBackgroundThread
            val notes = context.notesDB.getNotesInNotebook(notebookId)
            val now = System.currentTimeMillis()
            if (context.config.useRecycleBin) {
                notes.forEach { note ->
                    note.id?.let { context.widgetsDB.deleteNoteWidgets(it) }
                }
                context.widgetsDB.deleteNotebookWidgets(notebookId)
                context.notesDB.markNotesDeletedInNotebook(notebookId, now)
                context.notebooksDB.updateDeletedTs(notebookId, now)
            } else {
                notes.forEach { note ->
                    note.id?.let { context.widgetsDB.deleteNoteWidgets(it) }
                    context.notesDB.deleteNote(note)
                }
                context.widgetsDB.deleteNotebookWidgets(notebookId)
                context.notebooksDB.deleteNotebook(notebook)
            }
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun restoreNote(note: Note, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            val noteId = note.id ?: return@ensureBackgroundThread
            val notebook = context.notebooksDB.getNotebookWithId(note.notebookId)
            when {
                notebook == null -> {
                    note.notebookId = 1L
                    note.deletedTs = 0L
                    context.notesDB.insertOrUpdate(note)
                }
                notebook.isDeleted() -> {
                    context.notebooksDB.restoreNotebook(notebook.id!!)
                    context.notesDB.restoreNote(noteId)
                }
                else -> context.notesDB.restoreNote(noteId)
            }
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun restoreNotebook(notebook: Notebook, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            val notebookId = notebook.id ?: return@ensureBackgroundThread
            val deletedTs = notebook.deletedTs
            context.notebooksDB.restoreNotebook(notebookId)
            if (deletedTs > 0L) {
                context.notesDB.restoreNotesInNotebook(notebookId, deletedTs)
            }
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun permanentlyDeleteNote(note: Note, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            context.notesDB.deleteNote(note)
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun permanentlyDeleteNotebook(notebook: Notebook, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            val notebookId = notebook.id ?: return@ensureBackgroundThread
            context.notesDB.permanentlyDeleteNotesInNotebook(notebookId)
            context.notebooksDB.deleteNotebook(notebook)
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun emptyRecycleBin(callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            context.notesDB.permanentlyDeleteAllDeletedNotes()
            context.notebooksDB.permanentlyDeleteAllDeletedNotebooks()
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun emptyOldItems(callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            val cutoff = System.currentTimeMillis() - RECYCLE_BIN_RETENTION_MS
            context.notesDB.permanentlyDeleteNotesOlderThan(cutoff)
            context.notebooksDB.permanentlyDeleteNotebooksOlderThan(cutoff)
            callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
        }
    }

    fun hasItems(callback: (Boolean) -> Unit) {
        ensureBackgroundThread {
            val hasItems = context.notesDB.getDeletedNoteCount() > 0 || context.notebooksDB.getDeletedNotebookCount() > 0
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                callback(hasItems)
            }
        }
    }
}
