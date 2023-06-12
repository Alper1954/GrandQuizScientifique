package aperr.android.grandquizscientifique.screens

import android.app.AlertDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.databinding.FragmentGameBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GameFragment : Fragment(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private lateinit var binding: FragmentGameBinding
    private var catid = 0
    private lateinit var catname: String

    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//Modification de l'action du bouton Retour pour quitter l'application
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    exitDialog()
                }
            })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Log.i("alain", "GameFragment onCreateView")

        val safeArgs: GameFragmentArgs by navArgs()
        catid = safeArgs.catid
        catname = safeArgs.catname

        binding = FragmentGameBinding.inflate(inflater, container, false)


        when (catid) {
            0 -> binding.image.setImageResource(R.drawable.science)
            1 -> binding.image.setImageResource(R.drawable.physique)
            2 -> binding.image.setImageResource(R.drawable.chimie)
            3 -> binding.image.setImageResource(R.drawable.biologie)
            4 -> binding.image.setImageResource(R.drawable.astronomie)
            5 -> binding.image.setImageResource(R.drawable.botanique)
            6 -> binding.image.setImageResource(R.drawable.geologie)
        }


        binding.contentGame.apply {
            lifecycleOwner = viewLifecycleOwner
            roomViewModel = viewModel
            gameFragment = this@GameFragment
        }

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        NavigationUI.setupWithNavController(
            binding.collapsingToolbarLayout,
            binding.toolBar,
            navController,
            appBarConfiguration
        )


        viewModel.soundSetting.observe(viewLifecycleOwner) {
            if (it) {
                //If sound setting is set: Initialize tts and "stop sound" button is visible
                //Log.i("alain", "préférence sound setting is on")
                viewModel.ttsInitDone(false)
                binding.fab.visibility = View.VISIBLE
                tts = TextToSpeech(requireContext(), this)
            } else {
                //If sound setting is not set: "stop sound" button is invisible and free tts resources if necessary
                //Log.i("alain", "préférence sound is off")
                binding.fab.visibility = View.INVISIBLE

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
            }
        }





        viewModel.ttsInitDone.observe(viewLifecycleOwner) {
            //Log.i("alain", "ttsInitDone is called")
            if (it && viewModel.ttsEnableOutput.value!!) {
                speakout()
            }
        }

        viewModel.ttsEnableOutput.observe(viewLifecycleOwner) {
            //Log.i("alain", "ttsEnableOutput is called")
            if (it && viewModel.ttsInitDone.value!!) {
                speakout()
            }
        }

        binding.fab.setOnClickListener {
            viewModel.updateSound(false, requireContext())
            Snackbar.make(binding.fab, getString(R.string.sound_off), Snackbar.LENGTH_SHORT)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.timeout.collect {
                    Snackbar.make(
                        binding.contentGame.questionText, getString(R.string.timeout),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    /* le flag ttsEnableOutput est mis à false après le traitement de la question par tts pour éviter
       qu'une même question soit énoncée plusieurs fois par tts.
     */
    private fun speakout() {
        //i("alain", "speakout() is called ")
        tts?.speak(
            viewModel.currentQuestion.value?.questionText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
        tts?.speak(
            getString(R.string.answer1),
            TextToSpeech.QUEUE_ADD,
            null,
            null
        )
        tts?.speak(viewModel.currentQuestion.value?.answer1Text, TextToSpeech.QUEUE_ADD, null, null)
        tts?.speak(
            getString(R.string.answer2),
            TextToSpeech.QUEUE_ADD,
            null,
            null
        )
        tts?.speak(viewModel.currentQuestion.value?.answer2Text, TextToSpeech.QUEUE_ADD, null, null)
        if (viewModel.currentQuestion.value?.numberAnswers != 2) {
            tts?.speak(
                getString(R.string.answer3),
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
            tts?.speak(
                viewModel.currentQuestion.value?.answer3Text,
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
        }
        if (viewModel.currentQuestion.value?.numberAnswers == 4) {
            tts?.speak(
                getString(R.string.answer4),
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
            tts?.speak(
                viewModel.currentQuestion.value?.answer4Text,
                TextToSpeech.QUEUE_ADD,
                null,
                null
            )
        }
        viewModel.ttsDisableOutput()
    }


    //onInit() is called by TTS when it is initialized
    // Set initSoundStatus to true if initialization is OK
    override fun onInit(initStatus: Int) {
        if (initStatus == TextToSpeech.SUCCESS) {
            //Log.i("alain", "TTS init OK")
            viewModel.ttsInitDone(true)
        } else {
            //Log.i("alain", "TTS init ERROR!!!!")
            Snackbar.make(binding.fab, getString(R.string.sound_error), Snackbar.LENGTH_LONG)
                .show()
        }
    }


    override fun onDestroy() {
        //Log.i("alain", "GameFragment Destroy")
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    fun txtNumQuestion(numQuestion: Int): String {
        return getString(R.string.numQuestion, numQuestion)
    }

    fun txtScore(score: Int): String {
        return getString(R.string.score, score)
    }

    fun valid() {
        if (viewModel.valid()) {
            if (viewModel.ttsInitDone.value!!) {
                tts?.speak(
                    getString(R.string.good),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        } else {
            if (viewModel.ttsInitDone.value!!) {
                tts?.speak(
                    getString(R.string.error),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }
    }


    fun callExplication() {
        //Stop the sound if it is present
        tts?.speak("", TextToSpeech.QUEUE_FLUSH, null, null)

        val action = GameFragmentDirections.actionGameFragmentToExplicationFragment(catid, catname)
        findNavController().navigate(action)
    }

    fun backgroundAnswer(numAnswer: Int, state: Int): Int =
        when (state) {
            0 -> {  // state=0 -> No answer selected
                R.drawable.answers_box
            }

            1 -> {  // state=1 -> One answer is selected
                if (numAnswer == viewModel.selectedAnswer.value) {
                    R.drawable.answers_box_sel
                } else {
                    R.drawable.answers_box
                }
            }

            in 2..3 -> {  // state=2 or state=3 -> After validation
                if (numAnswer == viewModel.goodAnswer.value) {
                    R.drawable.answers_box_good
                } else if ((numAnswer == viewModel.goodAnswer.value) && (numAnswer == viewModel.selectedAnswer.value)) {
                    R.drawable.answers_box_good
                } else if ((numAnswer != viewModel.goodAnswer.value) && (numAnswer == viewModel.selectedAnswer.value)) {
                    R.drawable.answers_box_bad
                } else {
                    R.drawable.answers_box
                }
            }

            else -> {  // state=4 or state=5 (timeout)
                if (numAnswer == viewModel.selectedAnswer.value) {
                    R.drawable.answers_box_sel
                } else {
                    R.drawable.answers_box
                }
            }
        }

    fun finQuiz() {
        //val action = GameFragmentDirections.
        val action = GameFragmentDirections.actionGameFragmentToFinQuiz(catid, catname)
        findNavController().navigate(action)
    }

    fun exitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage(R.string.confirm)
            setPositiveButton(R.string.yes) { _,_ ->
                requireActivity().finish()
            }
            setNegativeButton(R.string.non) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        alertDialog.show()
    }


}











