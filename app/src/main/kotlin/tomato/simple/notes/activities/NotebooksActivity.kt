package tomato.simple.notes.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import tomato.simple.notes.R
import tomato.simple.notes.adapters.NotebooksAdapter
import tomato.simple.notes.adapters.SearchResultsAdapter
import tomato.simple.notes.databinding.ActivityNotebooksBinding
import tomato.simple.notes.dialogs.NewNotebookDialog
import tomato.simple.notes.dialogs.RenameNotebookDialog
import tomato.simple.notes.dialogs.SetNotebookPasswordDialog
import tomato.simple.notes.dialogs.UnlockNotebookPasswordDialog
import tomato.simple.notes.extensions.config
import tomato.simple.notes.extensions.notesDB
import tomato.simple.notes.extensions.notebooksDB
import tomato.simple.notes.helpers.OPEN_NEW_NOTE_DIALOG
import tomato.simple.notes.helpers.NOTEBOOK_ID
import tomato.simple.notes.helpers.NoteSearchHelper
import tomato.simple.notes.helpers.OPEN_NOTE_ID
import tomato.simple.notes.helpers.NotebooksHelper
import tomato.simple.notes.helpers.NotesHelper
import tomato.simple.notes.helpers.RecycleBinHelper
import tomato.simple.notes.models.Note
import tomato.simple.notes.models.Notebook
import tomato.simple.notes.models.NoteType

class NotebooksActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityNotebooksBinding::inflate)
    private var adapter: NotebooksAdapter? = null
    private var searchAdapter: SearchResultsAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var searchVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateMaterialActivityViews(binding.notebooksCoordinator, null, useTransparentNavigation = false, useTopSearchMenu = false)

        binding.notebooksToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    toggleSearch()
                    true
                }
                R.id.filter_by_tag -> {
                    filterByTag()
                    true
                }
                R.id.recycle_bin -> {
                    startActivity(Intent(this, RecycleBinActivity::class.java))
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.about -> {
                    launchAbout()
                    true
                }
                else -> false
            }
        }

        binding.notebooksList.layoutManager = GridLayoutManager(this, config.notebookColumns)

        adapter = NotebooksAdapter(
            mutableListOf(),
            itemClick = { openNotebook(it) },
            itemLongClick = { showNotebookActions(it) },
            dragStart = { viewHolder -> itemTouchHelper?.startDrag(viewHolder) },
            itemsReordered = { persistNotebookOrder(it) }
        )
        binding.notebooksList.adapter = adapter

        searchAdapter = SearchResultsAdapter(emptyList()) { result ->
            openSearchResult(result)
        }
        binding.searchResultsList.layoutManager = LinearLayoutManager(this)
        binding.searchResultsList.adapter = searchAdapter
        binding.notebooksSearch.onTextChangeListener { query ->
            performSearch(query)
        }

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                return adapter?.onItemMove(fromPosition, toPosition) == true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                adapter?.onDragFinished()
            }
        })
        itemTouchHelper?.attachToRecyclerView(binding.notebooksList)

        binding.newNotebookFab.setOnClickListener {
            NewNotebookDialog(this) { notebook ->
                NotebooksHelper(this).insertOrUpdateNotebook(notebook) { id ->
                    notebook.id = id
                    createInitialNoteIfNeeded(id) {
                        openNotebookUnlocked(notebook, promptForFirstNote = true)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.notebooksToolbar)

        (binding.notebooksList.layoutManager as? GridLayoutManager)?.spanCount = config.notebookColumns
        ensureDefaultNotebookExists {
            refreshNotebooks()
        }
        RecycleBinHelper(this).emptyOldItems()
        updateTextColors(binding.notebooksCoordinator)
    }

    private fun ensureDefaultNotebookExists(callback: () -> Unit) {
        val generalNoteTitle = getString(R.string.general_note)
        ensureBackgroundThread {
            val existingNotebook = notebooksDB.getNotebookWithId(1L)
            when {
                existingNotebook == null -> {
                    notebooksDB.insertOrUpdate(Notebook(id = 1L, title = generalNoteTitle, protectionType = PROTECTION_NONE, protectionHash = ""))
                }

                existingNotebook.title != generalNoteTitle -> {
                    existingNotebook.title = generalNoteTitle
                    notebooksDB.insertOrUpdate(existingNotebook)
                }
            }

            notesDB.insertNoteIfNotebookEmpty(
                notebookId = 1L,
                title = generalNoteTitle,
                value = "",
                type = NoteType.TYPE_TEXT.value,
                path = "",
                protectionType = PROTECTION_NONE,
                protectionHash = ""
            )
            notesDB.deleteDuplicateEmptyNotesInNotebook(
                notebookId = 1L,
                title = generalNoteTitle,
                type = NoteType.TYPE_TEXT.value
            )

            runOnUiThread(callback)
        }
    }

    private fun refreshNotebooks() {
        NotebooksHelper(this).getNotebooks { notebooks ->
            adapter?.updateItems(notebooks)
        }
    }

    private fun openNotebook(notebook: Notebook) {
        if (notebook.isLocked()) {
            UnlockNotebookPasswordDialog(this, notebook.protectionHash) {
                openNotebookUnlocked(notebook)
            }
        } else {
            openNotebookUnlocked(notebook)
        }
    }

    private fun openNotebookUnlocked(notebook: Notebook) {
        config.currentNotebookId = notebook.id ?: 1L
        Intent(this, MainActivity::class.java).apply {
            putExtra(NOTEBOOK_ID, config.currentNotebookId)
            startActivity(this)
        }
    }

    private fun openNotebookUnlocked(notebook: Notebook, promptForFirstNote: Boolean) {
        config.currentNotebookId = notebook.id ?: 1L
        Intent(this, MainActivity::class.java).apply {
            putExtra(NOTEBOOK_ID, config.currentNotebookId)
            if (promptForFirstNote) {
                putExtra(OPEN_NEW_NOTE_DIALOG, true)
            }
            startActivity(this)
        }
    }

    private fun showNotebookActions(notebook: Notebook) {
        val options = mutableListOf<String>().apply {
            add(getString(R.string.rename_notebook))
            add(if (notebook.isPinned()) getString(R.string.unpin_notebook) else getString(R.string.pin_notebook))
            add(if (notebook.isLocked()) getString(R.string.unlock_notebook) else getString(R.string.lock_notebook))
            if (notebook.id != 1L) {
                add(getString(R.string.delete_notebook))
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (options[which]) {
                    getString(R.string.rename_notebook) -> {
                        RenameNotebookDialog(this, notebook) {
                            refreshNotebooks()
                        }
                    }
                    getString(R.string.pin_notebook),
                    getString(R.string.unpin_notebook) -> togglePinned(notebook)
                    getString(R.string.lock_notebook) -> lockNotebook(notebook)
                    getString(R.string.unlock_notebook) -> unlockNotebook(notebook)
                    getString(R.string.delete_notebook) -> deleteNotebook(notebook)
                }
            }
            .show()
    }

    private fun togglePinned(notebook: Notebook) {
        val notebookId = notebook.id ?: return
        ensureBackgroundThread {
            val newPinned = if (notebook.isPinned()) 0 else 1
            val maxSortOrder = notebooksDB.getMaxSortOrder(newPinned) ?: 0
            val newSortOrder = maxSortOrder + 1
            notebooksDB.updatePinned(notebookId, newPinned)
            notebooksDB.updateSortOrder(notebookId, newSortOrder)
            runOnUiThread {
                refreshNotebooks()
            }
        }
    }

    private fun persistNotebookOrder(notebooks: List<Notebook>) {
        ensureBackgroundThread {
            val pinnedNotebooks = notebooks.filter { it.isPinned() }
            val unpinnedNotebooks = notebooks.filterNot { it.isPinned() }

            pinnedNotebooks.forEachIndexed { index, item ->
                val id = item.id ?: return@forEachIndexed
                val newOrder = index + 1
                if (item.sortOrder != newOrder) {
                    item.sortOrder = newOrder
                    notebooksDB.updateSortOrder(id, newOrder)
                }
            }

            unpinnedNotebooks.forEachIndexed { index, item ->
                val id = item.id ?: return@forEachIndexed
                val newOrder = index + 1
                if (item.sortOrder != newOrder) {
                    item.sortOrder = newOrder
                    notebooksDB.updateSortOrder(id, newOrder)
                }
            }
        }
    }

    private fun deleteNotebook(notebook: Notebook) {
        if (notebook.id == 1L) {
            toast(R.string.cannot_delete_default_notebook)
            return
        }

        val notebookTitle = notebook.title
        val message = String.format(getString(R.string.delete_notebook_prompt_message), notebookTitle)
        com.simplemobiletools.commons.dialogs.ConfirmationDialog(
            this,
            message,
            0,
            com.simplemobiletools.commons.R.string.ok,
            com.simplemobiletools.commons.R.string.cancel
        ) {
            val notebookId = notebook.id ?: return@ConfirmationDialog
            RecycleBinHelper(this).deleteNotebook(notebook) {
                if (config.useRecycleBin) {
                    toast(R.string.moved_to_recycle_bin)
                }
                if (config.currentNotebookId == notebookId) {
                    config.currentNotebookId = 1L
                }
                refreshNotebooks()
            }
        }
    }

    private fun lockNotebook(notebook: Notebook) {
        com.simplemobiletools.commons.dialogs.ConfirmationDialog(
            this,
            "",
            R.string.locking_warning,
            com.simplemobiletools.commons.R.string.ok,
            com.simplemobiletools.commons.R.string.cancel
        ) {
            SetNotebookPasswordDialog(this) { hash ->
                notebook.protectionHash = hash
                notebook.protectionType = 1
                NotebooksHelper(this).insertOrUpdateNotebook(notebook) {
                    refreshNotebooks()
                }
            }
        }
    }

    private fun unlockNotebook(notebook: Notebook) {
        UnlockNotebookPasswordDialog(this, notebook.protectionHash) {
            removeProtection(notebook)
        }
    }

    private fun removeProtection(notebook: Notebook) {
        notebook.protectionHash = ""
        notebook.protectionType = PROTECTION_NONE
        NotebooksHelper(this).insertOrUpdateNotebook(notebook) {
            refreshNotebooks()
        }
    }

    private fun filterByTag() {
        ensureBackgroundThread {
            val tags = NoteSearchHelper(this).allTags()
            runOnUiThread {
                if (tags.isEmpty()) {
                    toast(R.string.no_tags_yet)
                    return@runOnUiThread
                }

                val items = ArrayList<com.simplemobiletools.commons.models.RadioItem>()
                tags.forEachIndexed { index, tag ->
                    items.add(com.simplemobiletools.commons.models.RadioItem(index, tag))
                }
                com.simplemobiletools.commons.dialogs.RadioGroupDialog(this, items) {
                    val tag = tags[it as Int]
                    searchVisible = true
                    binding.notebooksSearch.beVisible()
                    binding.notebooksSearch.setText(tag)
                    ensureBackgroundThread {
                        val results = NoteSearchHelper(this).notesWithTag(tag)
                        runOnUiThread {
                            searchAdapter?.updateItems(results)
                            binding.notebooksList.beGone()
                            binding.searchResultsList.beVisible()
                            binding.newNotebookFab.beGone()
                        }
                    }
                }
            }
        }
    }

    private fun toggleSearch() {
        searchVisible = !searchVisible
        binding.notebooksSearch.beVisibleIf(searchVisible)
        if (searchVisible) {
            binding.notebooksSearch.requestFocus()
            showKeyboard(binding.notebooksSearch)
        } else {
            hideKeyboard()
            binding.notebooksSearch.setText("")
            showNotebooksList()
        }
    }

    private fun performSearch(query: String) {
        if (!searchVisible || query.isBlank()) {
            showNotebooksList()
            return
        }

        ensureBackgroundThread {
            val results = NoteSearchHelper(this).search(query)
            runOnUiThread {
                searchAdapter?.updateItems(results)
                binding.notebooksList.beGone()
                binding.searchResultsList.beVisible()
                binding.newNotebookFab.beGone()
            }
        }
    }

    private fun showNotebooksList() {
        binding.searchResultsList.beGone()
        binding.notebooksList.beVisible()
        binding.newNotebookFab.beVisible()
        searchAdapter?.updateItems(emptyList())
    }

    private fun openSearchResult(result: tomato.simple.notes.helpers.NoteSearchResult) {
        if (result.notebook != null) {
            openNotebook(result.notebook)
            return
        }

        val note = result.note ?: return
        NotebooksHelper(this).getNotebookWithId(note.notebookId) { notebook ->
            if (notebook == null) {
                return@getNotebookWithId
            }
            if (notebook.isLocked()) {
                UnlockNotebookPasswordDialog(this, notebook.protectionHash) {
                    openNote(note)
                }
            } else {
                openNote(note)
            }
        }
    }

    private fun openNote(note: Note) {
        config.currentNotebookId = note.notebookId
        Intent(this, MainActivity::class.java).apply {
            putExtra(NOTEBOOK_ID, note.notebookId)
            putExtra(OPEN_NOTE_ID, note.id)
            startActivity(this)
        }
    }

    private fun createInitialNoteIfNeeded(notebookId: Long, callback: () -> Unit) {
        val note = Note(
            id = null,
            notebookId = notebookId,
            title = getString(R.string.general_note),
            value = "",
            type = NoteType.TYPE_TEXT,
            path = "",
            protectionType = PROTECTION_NONE,
            protectionHash = ""
        )

        NotesHelper(this).insertOrUpdateNote(note) {
            callback()
        }
    }
}
