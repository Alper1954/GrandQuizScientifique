package aperr.android.grandquizscientifique.database

import androidx.room.Dao
import androidx.room.Query


@Dao
interface QuizDataBaseDao {


    @Query("SELECT questionId FROM quiz_question_table ORDER BY questionId")
    suspend fun getListeId(): List<Int>

    @Query("SELECT questionId FROM quiz_question_table WHERE question_type =:type")
    suspend fun getListeIdType(type: Int): List<Int>

    @Query("SELECT * FROM quiz_question_table WHERE questionId =:id" )
    suspend fun getQuestion(id: Int): QuizQuestion?

}