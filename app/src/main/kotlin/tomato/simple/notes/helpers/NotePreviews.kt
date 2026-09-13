package tomato.simple.notes.helpers

import android.content.Context
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tomato.simple.notes.extensions.config
import tomato.simple.notes.models.ChecklistItem
import tomato.simple.notes.models.CounterItem
import tomato.simple.notes.models.Note
import tomato.simple.notes.models.NoteType

fun Note.previewText(context: Context): CharSequence? {
    if (isLocked()) {
        return null
    }

    return when (type) {
        NoteType.TYPE_TEXT -> getNoteStoredValue(context)?.trim()?.ifEmpty { null }
        NoteType.TYPE_CHECKLIST -> {
            val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
            var items = Gson().fromJson<List<ChecklistItem>>(getNoteStoredValue(context), checklistItemType) ?: listOf()
            items = ChecklistItem.sorted(
                items.filter { it.title != null },
                context.config.getChecklistSorting(id),
                context.config.moveDoneChecklistItems
            )
            if (items.isEmpty()) {
                return null
            }
            val linePrefix = "• "
            val stringifiedItems = items.joinToString(separator = System.lineSeparator()) {
                "${linePrefix}${it.title}"
            }
            val formattedText = SpannableString(stringifiedItems)
            var currentPos = 0
            items.forEach { item ->
                currentPos += linePrefix.length
                if (item.isDone) {
                    formattedText.setSpan(StrikethroughSpan(), currentPos, currentPos + item.title.length, 0)
                }
                currentPos += item.title.length
                currentPos += System.lineSeparator().length
            }
            formattedText
        }
        NoteType.TYPE_COUNTER -> {
            val counterItemType = object : TypeToken<List<CounterItem>>() {}.type
            val rawValue = getNoteStoredValue(context)?.ifEmpty { "[]" } ?: "[]"
            val items = Gson().fromJson<List<CounterItem>>(rawValue, counterItemType) ?: listOf()
            items.joinToString(separator = System.lineSeparator()) { "${it.title}: ${it.count}" }.ifBlank { null }
        }
    }
}
