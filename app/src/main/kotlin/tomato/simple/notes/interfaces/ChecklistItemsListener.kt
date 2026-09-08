package tomato.simple.notes.interfaces

interface ChecklistItemsListener {
    fun refreshItems()

    fun saveChecklist(callback: () -> Unit = {})

    fun migrateChecklistItems(itemIds: List<Int>)

    fun moveEntireNote()
}
