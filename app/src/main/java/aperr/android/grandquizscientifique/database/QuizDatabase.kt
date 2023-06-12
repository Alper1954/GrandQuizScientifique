package aperr.android.grandquizscientifique.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.Locale

@Database(entities = [QuizQuestion::class], version = 1, exportSchema = false)
abstract class QuizDatabase : RoomDatabase() {

    abstract val quizDataBaseDao: QuizDataBaseDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null
        private var database_file = if(Locale.getDefault().language == "fr") "quiz_database_fr.db" else "quiz_database_en.db"
        fun getDatabase(
            context: Context,
        ): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                //Log.i("alain", "start getDatabase")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    database_file
                )
                    .createFromAsset(database_file)
                    .build()
                //Log.i("alain", "end getDatabase")
                INSTANCE = instance
                instance
            }
        }
    }
}