package tomato.simple.notes.models

import com.simplemobiletools.commons.helpers.SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import tomato.simple.notes.helpers.CollatorBasedComparator
import tomato.simple.notes.helpers.SORT_BY_DONE
import kotlinx.serialization.Serializable

@Serializable
data class ChecklistItem(
    val id: Int,
    val dateCreated: Long = 0L,
    var title: String,
    var isDone: Boolean
) : Comparable<ChecklistItem> {

    companion object {
        var sorting = 0
    }

    override fun compareTo(other: ChecklistItem): Int {
        var result = when {
            sorting and SORT_BY_DONE != 0 -> isDone.compareTo(other.isDone)
            sorting and SORT_BY_TITLE != 0 -> CollatorBasedComparator().compare(title, other.title)
            else -> dateCreated.compareTo(other.dateCreated)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }
}
