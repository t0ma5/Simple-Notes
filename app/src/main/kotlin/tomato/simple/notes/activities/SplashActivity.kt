package tomato.simple.notes.activities

import android.content.Intent
import com.simplemobiletools.commons.activities.BaseSplashActivity
import tomato.simple.notes.helpers.OPEN_NOTE_ID

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        if (intent.extras?.containsKey(OPEN_NOTE_ID) == true) {
            Intent(this, MainActivity::class.java).apply {
                putExtra(OPEN_NOTE_ID, intent.getLongExtra(OPEN_NOTE_ID, -1L))
                startActivity(this)
            }
        } else {
            startActivity(Intent(this, NotebooksActivity::class.java))
        }
        finish()
    }
}
