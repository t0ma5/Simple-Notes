package tomato.simple.notes.interfaces

import tomato.simple.notes.models.ChecklistItem

interface ChecklistItemsListener {
    fun refreshItems()

    fun saveChecklist(callback: () -> Unit = {})

    fun migrateChecklistItems(itemIds: List<Int>)

    fun moveEntireNote()

    fun captureHistory()

    fun onItemsReordered(reorderedItems: List<ChecklistItem>) {}
}
