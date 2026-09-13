package tomato.simple.notes

import android.app.Application
import com.simplemobiletools.commons.extensions.checkUseEnglish
import tomato.simple.notes.extensions.config

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        config.applyDefaultDarkRedThemeIfNeeded()
    }
}
