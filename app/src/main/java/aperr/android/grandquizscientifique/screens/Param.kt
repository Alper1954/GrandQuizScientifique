package aperr.android.grandquizscientifique.screens

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.databinding.FragmentParamBinding
import com.google.android.material.snackbar.Snackbar

class Param : Fragment() {

    private lateinit var binding: FragmentParamBinding

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Snackbar.make(binding.sound, getString(R.string.sound_error), Snackbar.LENGTH_LONG)
                    .show()
                binding.sound.isChecked = false
            }
        }

    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParamBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.roomViewModel = viewModel

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.timer.setOnCheckedChangeListener { _, b ->
            viewModel.updateTimer(b, requireContext())
        }

        binding.sound.setOnCheckedChangeListener { _, b ->
            Log.i("alain", "change setting sound: $b")
            viewModel.updateSound(b, requireContext())
        }

        viewModel.soundSetting.observe(viewLifecycleOwner){
            if(it){
                val checkIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
                startForResult.launch(checkIntent)
            }
        }


        return binding.root
    }

}