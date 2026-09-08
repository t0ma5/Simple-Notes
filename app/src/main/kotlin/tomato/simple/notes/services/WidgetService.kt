package tomato.simple.notes.services

import android.content.Intent
import android.widget.RemoteViewsService
import tomato.simple.notes.adapters.WidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = WidgetAdapter(applicationContext, intent)
}
