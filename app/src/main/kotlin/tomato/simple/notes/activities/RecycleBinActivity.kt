package tomato.simple.notes.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.extensions.viewBinding
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import tomato.simple.notes.R
import tomato.simple.notes.adapters.RecycleBinAdapter
import tomato.simple.notes.databinding.ActivityRecycleBinBinding
import tomato.simple.notes.extensions.notesDB
import tomato.simple.notes.extensions.notebooksDB
import tomato.simple.notes.helpers.RecycleBinHelper
import tomato.simple.notes.models.RecycleBinItem

class RecycleBinActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityRecycleBinBinding::inflate)
    private var adapter: RecycleBinAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateMaterialActivityViews(binding.recycleBinCoordinator, null, useTransparentNavigation = false, useTopSearchMenu = false)

        adapter = RecycleBinAdapter(emptyList(), restoreClick = { restoreItem(it) }, deleteClick = { deleteItem(it) })
        binding.recycleBinList.layoutManager = LinearLayoutManager(this)
        binding.recycleBinList.adapter = adapter

        binding.recycleBinToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.empty_recycle_bin -> {
                    emptyBin()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.recycleBinToolbar, NavigationIcon.Arrow)
        updateTextColors(binding.recycleBinCoordinator)
        binding.recycleBinEmpty.setTextColor(getProperTextColor())
        refreshItems()
    }

    private fun refreshItems() {
        ensureBackgroundThread {
            val notebooks = notebooksDB.getDeletedNotebooks()
            val notes = notesDB.getDeletedNotes()
            val liveNotebooks = notebooksDB.getNotebooks().associateBy { it.id }
            val deletedNotebooks = notebooks.associateBy { it.id }

            val items = mutableListOf<RecycleBinItem>()
            notebooks.forEach { notebook ->
                items.add(
                    RecycleBinItem(
                        notebook = notebook,
                        title = notebook.title,
                        subtitle = getString(R.string.deleted_notebook)
                    )
                )
            }
            notes.forEach { note ->
                val deletedNotebook = deletedNotebooks[note.notebookId]
                if (deletedNotebook != null && note.deletedTs == deletedNotebook.deletedTs) {
                    return@forEach
                }
                val notebookTitle = liveNotebooks[note.notebookId]?.title
                    ?: deletedNotebook?.title
                    ?: getString(R.string.notebooks)
                items.add(
                    RecycleBinItem(
                        note = note,
                        title = note.title,
                        subtitle = getString(R.string.deleted_note_in_notebook, notebookTitle)
                    )
                )
            }

            runOnUiThread {
                adapter?.updateItems(items)
                binding.recycleBinEmpty.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun restoreItem(item: RecycleBinItem) {
        val helper = RecycleBinHelper(this)
        if (item.notebook != null) {
            helper.restoreNotebook(item.notebook) { refreshItems() }
        } else if (item.note != null) {
            helper.restoreNote(item.note) { refreshItems() }
        }
    }

    private fun deleteItem(item: RecycleBinItem) {
        ConfirmationDialog(this, "", R.string.delete_permanently_confirm, com.simplemobiletools.commons.R.string.delete, com.simplemobiletools.commons.R.string.cancel) {
            val helper = RecycleBinHelper(this)
            if (item.notebook != null) {
                helper.permanentlyDeleteNotebook(item.notebook) { refreshItems() }
            } else if (item.note != null) {
                helper.permanentlyDeleteNote(item.note) { refreshItems() }
            }
        }
    }

    private fun emptyBin() {
        ConfirmationDialog(this, "", R.string.empty_recycle_bin_confirm, com.simplemobiletools.commons.R.string.delete, com.simplemobiletools.commons.R.string.cancel) {
            RecycleBinHelper(this).emptyRecycleBin {
                toast(R.string.recycle_bin_emptied)
                refreshItems()
            }
        }
    }
}
