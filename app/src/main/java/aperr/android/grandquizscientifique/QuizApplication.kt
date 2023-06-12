package aperr.android.grandquizscientifique

import android.app.Application
import aperr.android.grandquizscientifique.database.QuizDatabase

class QuizApplication: Application() {
    val database by lazy { QuizDatabase.getDatabase(this) }
    val userPref by lazy {SettingsDataStore(this.dataStore)}
}