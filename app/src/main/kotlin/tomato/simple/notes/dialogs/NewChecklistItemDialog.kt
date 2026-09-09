package tomato.simple.notes.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.DARK_GREY
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import tomato.simple.notes.R
import tomato.simple.notes.databinding.DialogNewChecklistItemBinding
import tomato.simple.notes.databinding.ItemAddChecklistBinding
import tomato.simple.notes.extensions.config

class NewChecklistItemDialog(val activity: Activity, private val noteId: Long? = null, callback: (titles: ArrayList<String>) -> Unit) {
    private val titles = mutableListOf<AppCompatEditText>()
    private val binding = DialogNewChecklistItemBinding.inflate(activity.layoutInflater)
    private val view = binding.root

    init {
        addNewEditText()
        val plusTextColor = if (activity.isWhiteTheme()) {
            DARK_GREY
        } else {
            activity.getProperPrimaryColor().getContrastColor()
        }

        binding.apply {
            addItem.applyColorFilter(plusTextColor)
            addItem.setOnClickListener {
                addNewEditText()
            }
            settingsAddChecklistTop.beVisibleIf(activity.config.getChecklistSorting(noteId) and SORT_BY_CUSTOM != 0)
            settingsAddChecklistTop.isChecked = activity.config.addNewChecklistItemsTop
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.add_new_checklist_items) { alertDialog ->
                    alertDialog.setCanceledOnTouchOutside(false)
                    alertDialog.showKeyboard(titles.first())
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        activity.config.addNewChecklistItemsTop = binding.settingsAddChecklistTop.isChecked
                        when {
                            titles.all { it.text!!.isEmpty() } -> activity.toast(com.simplemobiletools.commons.R.string.empty_name)
                            else -> {
                                val titles = titles.map { it.text.toString() }.filter { it.isNotEmpty() }.toMutableList() as ArrayList<String>
                                callback(titles)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun addNewEditText() {
        ItemAddChecklistBinding.inflate(activity.layoutInflater).apply {
            titleEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.KEYCODE_ENTER) {
                    addNewEditText()
                    true
                } else {
                    false
                }
            }
            titles.add(titleEditText)
            binding.checklistHolder.addView(this.root)
            activity.updateTextColors(binding.checklistHolder)
            binding.dialogHolder.post {
                binding.dialogHolder.fullScroll(View.FOCUS_DOWN)
                activity.showKeyboard(titleEditText)
            }
        }
    }
}
