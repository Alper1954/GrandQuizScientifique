package aperr.android.grandquizscientifique.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "quiz_question_table")
data class QuizQuestion(
    @PrimaryKey(autoGenerate = true)
    var questionId: Int = 0,

    @ColumnInfo(name = "question_type")
    var questionType: Int,

    @ColumnInfo(name = "question_text")
    var questionText: String,

    @ColumnInfo(name = "question_explication")
    var questionExplication: String,

    @ColumnInfo(name = "number_answers")
    var numberAnswers: Int,

    @ColumnInfo(name = "nbok_answer")
    var nbokAnswer: Int,

    @ColumnInfo(name = "answer1_text")
    var answer1Text: String = " ",

    @ColumnInfo(name = "answer2_text")
    var answer2Text: String = " ",

    @ColumnInfo(name = "answer3_text")
    var answer3Text: String = " ",

    @ColumnInfo(name = "answer4_text")
    var answer4Text: String = " "
)
