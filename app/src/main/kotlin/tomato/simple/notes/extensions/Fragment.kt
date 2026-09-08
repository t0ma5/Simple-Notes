package tomato.simple.notes.extensions

import androidx.fragment.app.Fragment
import tomato.simple.notes.helpers.Config

val Fragment.config: Config? get() = if (context != null) Config.newInstance(context!!) else null
