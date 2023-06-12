package aperr.android.grandquizscientifique

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

class SettingsDataStore(datastore: DataStore<Preferences>) {

    object PreferencesKeys {
        val USERNAME = stringPreferencesKey("username")
        val SOUND = booleanPreferencesKey("sound")
        val TIMER = booleanPreferencesKey("timer")
    }

    val prefFlow: Flow<Preferences> = datastore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    suspend fun updateUser(name: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = name
        }
    }

    suspend fun updateTimer(status: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIMER] = status
        }
    }

    suspend fun updateSound(status: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND] = status
        }
    }
}
