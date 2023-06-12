package aperr.android.grandquizscientifique.screens

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.SettingsDataStore.PreferencesKeys.SOUND
import aperr.android.grandquizscientifique.SettingsDataStore.PreferencesKeys.TIMER
import aperr.android.grandquizscientifique.SettingsDataStore.PreferencesKeys.USERNAME
import aperr.android.grandquizscientifique.database.QuizQuestion
import aperr.android.grandquizscientifique.network.WebPlayerApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val NUMBER_QUESTION = 20
private const val TIMEOUT = 61000L
private const val TIME_REFRESH = 1000L

data class Category(
    val id: Int,
    var picture: Int,
    var name: String,
    var background: Int
)

class RoomViewModel(
    private val application: QuizApplication
) : AndroidViewModel(application) {


//**************************************************************
//  VARIABLES DECLARATION
//**************************************************************

    /* Mysql Network Database Accessibility*/

    var networkAccessOk: Boolean = false
        private set


//***************************************************
// variables used for editing a username
//***************************************************

    /* checkingInput=true during the entered value validation phase
       used to display "Verification" in the Helper zone of the Text field
    */
    private val _checkingInput = MutableStateFlow(false)
    val checkingInput = _checkingInput.asStateFlow()


    /* resultValidInput: string event after entered value validation
              "0" -> validation is ok
              "1" -> name already existing
              "2" -> error in saving the name
    */
    private val _resultValidInput = MutableSharedFlow<String>()
    val resultValidInput = _resultValidInput.asSharedFlow()

//************************************************************
// User preference variables
//***********************************************************

    /* user name setting */
    private val _username: MutableLiveData<String> = MutableLiveData()
    val username: LiveData<String>
        get() = _username

    /* sound setting */
    private val _soundSetting: MutableLiveData<Boolean> = MutableLiveData(false)
    val soundSetting: LiveData<Boolean>
        get() = _soundSetting

    /* timer setting */
    private val _timerSetting: MutableLiveData<Boolean> = MutableLiveData(false)
    val timerSetting: LiveData<Boolean>
        get() = _timerSetting

//**************************************************************************************
// Various lists and variable used by the Quiz Question processing
//************************************************************************************

    /* List of all the Question categories: Physique, Astronomie, Chimie...
    */
    var listCat: MutableList<Category> = mutableListOf()

    /* List of the current Question categories used by a User
    */
    var listCatUser: MutableList<Category> = mutableListOf()

    private var _statusData: MutableLiveData<Int> = MutableLiveData()
    val statusData: LiveData<Int>
        get() = _statusData

    /* position of the selected category by the user in listCat
    */
    var catId = 0

    /*
      emptListId: event generated if ListId is empty
      goodListId: event generated after ListId creation
    */
    private val _emptyListId = MutableSharedFlow<String>()
    val emptyListId = _emptyListId.asSharedFlow()

    private val _goodListId = MutableSharedFlow<Category>()
    val goodListId = _goodListId.asSharedFlow()

    /* Initial list of All the Question Ids for a specific category
    */
    private var listId = listOf<Int>()

    /* Current shuffled list of the Question Ids for a specific category
    */
    private var currentListId = mutableListOf<Int>()

    /* Current Quiz Question selected in database for displaying
    */
    private val _currentQuestion: MutableLiveData<QuizQuestion> = MutableLiveData()
    val currentQuestion: LiveData<QuizQuestion>
        get() = _currentQuestion

    /* number of the current Question
    */
    private val _numQuestion: MutableLiveData<Int> = MutableLiveData()
    val numQuestion: LiveData<Int>
        get() = _numQuestion

    /* current Quiz Score
    */
    private val _score = MutableLiveData(0)
    val score: LiveData<Int>
        get() = _score


//******************************************************************************
// variables utilisées pour la gestion du timeout de réponse à une question
//******************************************************************************

    private var timerQuestion: CountDownTimer

    private val _currentTime: MutableLiveData<Long> = MutableLiveData()
    val currentTime: LiveData<Long>
        get() = _currentTime

    private val _timeout = MutableSharedFlow<String>()
    val timeout = _timeout.asSharedFlow()

//*************************************************************************
// variables used by tts processing
//**************************************************************************

    /* ttsInitDone: Flag indicates that tts initialization is in progress
    */
    private val _ttsInitDone: MutableLiveData<Boolean> = MutableLiveData(false)
    val ttsInitDone: LiveData<Boolean>
        get() = _ttsInitDone


    /* ttsEnableOutput: Flag indicates that a new question is ready to be processed by tts
    */
    private val _ttsEnableOutput: MutableLiveData<Boolean> = MutableLiveData(false)
    val ttsEnableOutput: LiveData<Boolean>
        get() = _ttsEnableOutput


//***********************************************************
//  variables related to the question answers
//***********************************************************

    /* selectedAnswer = Answer Number (1 to 4) selected by the user
    */
    private val _selectedAnswer = MutableLiveData<Int>()
    val selectedAnswer: LiveData<Int>
        get() = _selectedAnswer


    /* goodAnswer = Good Answer Number (1 to 4)) */
    private val _goodAnswer = MutableLiveData<Int>()
    val goodAnswer: LiveData<Int>
        get() = _goodAnswer


    /* state of the Quiz:
            0 -> No answer is selected
            1 -> One answer is selected: Valid button is visible
            2 -> Valid clicked and not last Question: Next and Explication buttons are visible
            3 -> Valid clicked and last Question: Fin du Quiz and Explication buttons are visible
            4 -> timeout and not last Question: Next and Explication buttons are visible
            5 -> timeout and last Question: Fin du Quiz is visible
    */
    private val _state = MutableLiveData<Int>()
    val state: LiveData<Int>
        get() = _state

    //**************************************************************************************
// Score results
//************************************************************************************
    /* List of the last score values */
    private var listScoreVal = mutableListOf<Int>()

    /* List of the last score qualities */
    private var listScoreQual = mutableListOf<Int>()

    /* List of the last scores has been updated */
    private var listScoreValid = false

//**************************************************************************
//  START OF CODE
//***************************************************************************

    /* Le bloc init est exécuté lors de l'instanciation de la classe RoomViewMode
       Ce bloc appelle la fonction collectPref() pour lire le DataStore et initiliser les préférences utiliseur,
       le bloc init crée l'object timer utilisé pour la gestion du temps mis pour répondre à une question
       le bloc init construit également la liste listCat en y ajoutant des objets de la classe Category.
     */
    init {
        //Log.i("alain", "init viewmodel")

        _numQuestion.value = 1
        _score.value = 0

        collectPref()

        timerQuestion = object : CountDownTimer(TIMEOUT, TIME_REFRESH) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                if (_numQuestion.value == NUMBER_QUESTION) {
                    _state.value = 5
                    if (networkAccessOk && (listScoreValid)) writeScore()

                } else {
                    _state.value = 4
                }
                viewModelScope.launch {
                    _timeout.emit("timeout")
                }
                _currentTime.value = 0
            }
        }


        listCat.add(
            Category(
                1,
                R.drawable.physique,
                application.resources.getString(R.string.physics),
                R.drawable.item_background_empty
            )
        )
        listCat.add(
            Category(
                2,
                R.drawable.astronomie,
                application.resources.getString(R.string.astronomy),
                R.drawable.item_background_empty
            )
        )
        listCat.add(
            Category(
                3,
                R.drawable.chimie,
                application.resources.getString(R.string.chemistry),
                R.drawable.item_background_empty
            )
        )
        listCat.add(
            Category(
                4,
                R.drawable.biologie,
                application.resources.getString(R.string.biology),
                R.drawable.item_background_empty
            )
        )
        listCat.add(
            Category(
                5,
                R.drawable.botanique,
                application.resources.getString(R.string.botany),
                R.drawable.item_background_empty
            )
        )
        listCat.add(
            Category(
                6,
                R.drawable.geologie,
                application.resources.getString(R.string.geology),
                R.drawable.item_background_empty
            )
        )
        listCat.add(
            Category(
                0,
                R.drawable.science,
                application.resources.getString(R.string.science),
                R.drawable.item_background_empty
            )
        )
    }

    /* La fonction collectPref() appelle la fonction collect du flow de type Preferences exposé par la fonction
       userPref.prefFlow de la classe SettingsDataStore pour initialiser les préférences utilisateur username, soundSetting
       et timerSetting.
    */
    private fun collectPref() {
        viewModelScope.launch {
            application.userPref.prefFlow.collect {
                _username.value = it[USERNAME]
                _soundSetting.value = it[SOUND] ?: false
                _timerSetting.value = it[TIMER] ?: false
            }
        }
    }

    /*
    Lors du démarrage du Quiz, on considère que l'accès réseau est opérationnel.
    Cette fonction est appelée dans la classe StartFragment
     */
    fun initNetorkAccess() {
        networkAccessOk = true
    }

    /* La fonction newPlayer appelle la fonction retrofit createPlayer qui vérifie si le nom saisi dans le text field
       n'existe pas déjà dansla base de données quiz_scientifique:
          - Si ce nom n'existe pas, elle enregistre ce nom dans la base de données quiz_scientifique, met à jour
       la préférence utilisateur "username" dans le DataStore et elle renvoie un événemement avec la valeur "0".
          - Si ce nom existe déjà, elle renvoie un événemement avec la valeur "1".
          - En cas d'erreur réseau ou en cas d'erreur d'accès à la base de données quiz_scientique, elle renvoie
       un événemement avec la valeur "2".
    */
    fun newPlayer(name: String, context: Context) {
        //Log.i("alain", "name: $name")
        _checkingInput.value =
            true    // Affichage du texte "verification" dans le helper du text field
        viewModelScope.launch {
            try {
                val status = WebPlayerApi.retrofitService.createPlayer(name)
                //Log.i("alain", "status: ${status.toString()}")
                _checkingInput.value = false // Effacement du texte "verification"
                if (status == "0") {
                    application.userPref.updateUser(name, context)
                }
                _resultValidInput.emit(status)

            } catch (e: Exception) {
                //Log.i("alain", "retrofit exception: ${e.message}")
                _checkingInput.value = false // Effacement du texte "verification"
                networkAccessOk = false
                _resultValidInput.emit("2")
            }
        }
    }


    /* Mise à jour de la préférence utilisateur "timer" dans le DataStore
    */
    fun updateTimer(timer: Boolean, context: Context) {
        viewModelScope.launch {
            application.userPref.updateTimer(timer, context)
        }
    }

    /* Mise à jour de la préférence utilisateur "sound" dans le DataStore
    */
    fun updateSound(sound: Boolean, context: Context) {
        viewModelScope.launch {
            application.userPref.updateSound(sound, context)
        }
    }

    /* Mis à jour du flag ttsInitDone qui indique que l'initialisation du tts est en cours
    */
    fun ttsInitDone(status: Boolean) {
        _ttsInitDone.value = status
    }

    /* démarrage du compteur de timeout si la préférence timerSetting est à true
     */
    private fun processTimeout() {

        if (_timerSetting.value == true) {
            timerQuestion.start()
        } else {
            timerQuestion.cancel()
        }
    }

    fun initListCatUser() {
        _statusData.value = 0
        if (networkAccessOk && (_username.value != null)) {
            viewModelScope.launch {
                try {
                    _statusData.value = 1

                    listCatUser = mutableListOf()
                    val result = WebPlayerApi.retrofitService.initFlags(_username.value!!)
                    val listResult: MutableList<String> = result.split(",").toMutableList()

                    val status = listResult[0]
                    if (status == "0") {
                        val listFlags = listResult.subList(1, 8).map { it.toInt() }.toMutableList()

                        listCatUser = mutableListOf()
                        for (i in listCat.indices) {
                            if (listFlags[i] == 1) {
                                listCatUser.add(listCat[i])
                            }
                        }
                        if (listCatUser.isNotEmpty()) {
                            _statusData.value = 2
                        } else {
                            _statusData.value = 0
                        }
                    } else {
                        networkAccessOk = false
                        _statusData.value = 0
                    }
                } catch (e: Exception) {
                    networkAccessOk = false
                    _statusData.value = 0
                }
            }
        }
    }


    /* Cette fonction est appelée lorsque l'utilisateur sélectionne une catégorie dans la liste des catégories:
       Construction de la liste currentListId qui est la liste initiale des Id de la catégorie sélectionnée:
       Si currentListId est vide: Renvoie d'un événement pour signaler que cette catégorie es vide
       Sinon:
       Recherche dans QuizDatabase de la question suivante et mise jour de CurrentQuestion.
       Le numéro de la question est initialisé à 1
       La variable ttsEnableOutput est mise à true pour signaler que la question est prête à être parlée par tts
       Appel de la fonction processTimeout() pour démarrer le timeout si l'option timersetting a été sélectionnée.
       Enfin, les listes contenant les scores sont initialisées avec les valeurs lues dans la base de données
       quiz_scientifique.
     */
    fun selectCat(pos: Int) {

        listScoreValid = false

        _state.value = 0
        _selectedAnswer.value = 0
        _goodAnswer.value = 0

        catId = listCat[pos].id

        viewModelScope.launch {
            listId =
                if (catId == 0) application.database.quizDataBaseDao.getListeId() else application.database.quizDataBaseDao.getListeIdType(
                    catId
                )
            //Log.i("alain", "categorie $catId: ${listId.size}")

            if (listId.isNotEmpty()) {
                currentListId = mutableListOf()
                currentListId.addAll(listId)
                currentListId.shuffle()

                _currentQuestion.value = application.database.quizDataBaseDao.getQuestion(nextQuestionId())
                _numQuestion.value = 1
                _score.value = 0
                _ttsEnableOutput.value = true
                processTimeout()

                _goodListId.emit(listCat[pos])

            } else {
                _emptyListId.emit("empty")
            }
        }

        //Log.i("alain", "networkAccessOk: " + networkAccessOk.toString())

        if (networkAccessOk && (_username.value != null)) initScore()
    }

    /* La fonction initScore() initialise les listes contenant les scores réalisés avec les valeurs lues
    dans la base de données quiz_scientifique.
    Le String result renvoyé par la fonction retrofit readScore a pour format:
          result = "status,score1,...,score5,score1_qual,...,score5_qual"
    */
    private fun initScore() {
        viewModelScope.launch {
            try {
                val result = WebPlayerApi.retrofitService.initScore(_username.value!!, catId)
                //Log.i("alain", "result: " + result)

                val listResult: MutableList<String> = result.split(",").toMutableList()

                val status = listResult[0]
                if (status == "0") {
                    listScoreValid = true
                    listScoreVal = listResult.subList(1, 6).map { it.toInt() }.toMutableList()
                    listScoreQual = listResult.subList(6, 11).map { it.toInt() }.toMutableList()
//                    for (v in listScoreVal) {
//                        Log.i("alain","score= " + v)
//                    }
//                    for (q in listScoreQual) {
//                        Log.i("alain","score= " + q)
//                    }
                } else {
                    //Log.i("alain", "status= " + status)
                    networkAccessOk = false
                }

            } catch (e: Exception) {
                //Log.i("alain", "exception initScore: ${e.message}")
                networkAccessOk = false
            }
        }
    }

    /* La fonction getNextQuestion() est appelée lorsque l'utilisateur appuie sur le bouton next pour afficher la question suivante:
       Recherche dans QuizDatabase de la question suivante et mise jour de CurrentQuestion.
       Le numéro de la question est incrémenté
       La variable ttsEnableOutput est mise à true pour signaler que la nouvelle question est prête à être parlée par tts.
    */
    fun getNextQuestion() {
        processTimeout()
        _state.value = 0
        _selectedAnswer.value = 0
        _goodAnswer.value = 0
        viewModelScope.launch {
            //Log.i("alain", "coroutine dispatcher = $coroutineContext")
            _currentQuestion.value = application.database.quizDataBaseDao.getQuestion(nextQuestionId())
            _numQuestion.value = _numQuestion.value?.plus(1)
            _ttsEnableOutput.value = true
        }
    }


    /* Détermination de l'id de la question suivante: C'est l'id extrait de la liste currentListId si celle n'est
       pas vide
     */
    private fun nextQuestionId(): Int {
        if (currentListId.isEmpty()) {
            resetQuestionIdList()
        }
        return currentListId.removeAt(0)
    }


    /* Cette fonction est appelée quand la liste currentListId est vide. La liste currentListId est réinitialisée
       avec le contenu de listId puis est mélangée
     */
    private fun resetQuestionIdList() {
        currentListId = mutableListOf()
        currentListId.addAll(listId)
        currentListId.shuffle()
    }


    /* Cette fonction est appelée après le traitement de la question par tts pour mettre le flag ttsEnableOutput à false
       afin s'éviter qu'une même question soit parlée plusieurs fois par tts.
    */
    fun ttsDisableOutput() {
        _ttsEnableOutput.value = false
    }


    /* Cette fonction est appelée après la sélection par l'utilisateur de la question de numéro numAnswer.
       Mémorisation de ce numéro dans la variable selectedAnswer et la variable state est positionnée à 1
       pour rendre visible le bouton validation.
     */
    fun onAnswer(numAnswer: Int) {
        if (_state.value == 2) return
        _selectedAnswer.value = numAnswer
        _state.value = 1
    }

    /* Cette fonction est appelée lorsque l'utilisateur valide sa réponse et retourne true si la valeur
       sélectionnée est la bonne réponse.
       Le Quiz se termine si c'est la dernière question.
     */
    fun valid(): Boolean {
        timerQuestion.cancel()
        _goodAnswer.value = _currentQuestion.value!!.nbokAnswer
        if (_selectedAnswer.value == _goodAnswer.value) _score.value = _score.value!! + 1
        if (_numQuestion.value == NUMBER_QUESTION) {
            _state.value = 3
            if (networkAccessOk && (listScoreValid)) writeScore()
        } else {
            _state.value = 2
        }
        return (_selectedAnswer.value == _goodAnswer.value)
    }

    private fun writeScore() {
        listScoreVal.removeAt(0)
        listScoreVal.add(_score.value!!)
        listScoreQual.removeAt(0)
        listScoreQual.add(if (_timerSetting.value == true) 1 else 0)

        //for (v in listScoreVal) {
            //Log.i("alain","score= " + v)
        //}
        //for (q in listScoreQual) {
            //Log.i("alain","score= " + q)
        //}

        viewModelScope.launch {
            try {
                val status = WebPlayerApi.retrofitService.updateScore(
                    _username.value!!,
                    catId,
                    listScoreVal[4],
                    listScoreVal[3],
                    listScoreVal[2],
                    listScoreVal[1],
                    listScoreVal[0],
                    listScoreQual[4],
                    listScoreQual[3],
                    listScoreQual[2],
                    listScoreQual[1],
                    listScoreQual[0]
                )

                if (status != "0") {
                    //Log.i("alain", status)
                    networkAccessOk = false
                }


            } catch (e: Exception) {
                //Log.i("alain", "${e.message}")
                networkAccessOk = false
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        timerQuestion.cancel()
    }

}


class RoomViewModelFactory(
    private val application: QuizApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}