package tomato.simple.notes.extensions

import android.app.Activity
import com.simplemobiletools.commons.extensions.performSecurityCheck
import tomato.simple.notes.dialogs.UnlockNotebookPasswordDialog
import tomato.simple.notes.helpers.NotebookPasswordHasher
import tomato.simple.notes.models.Notebook

fun Activity.unlockNotebookIfNeeded(notebook: Notebook?, callback: () -> Unit) {
    if (notebook == null || !notebook.isLocked()) {
        callback()
        return
    }
    if (NotebookPasswordHasher.isLegacyHash(notebook.protectionHash)) {
        UnlockNotebookPasswordDialog(this, notebook.protectionHash, callback)
    } else {
        performSecurityCheck(
            protectionType = notebook.protectionType,
            requiredHash = notebook.protectionHash,
            successCallback = { _, _ -> callback() }
        )
    }
}
