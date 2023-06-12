package aperr.android.grandquizscientifique.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.databinding.FragmentFinQuizBinding
import com.google.android.material.snackbar.Snackbar


class FinQuiz : Fragment() {
    private lateinit var binding: FragmentFinQuizBinding

    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Log.i("alain", "FinQuiz onCreateView")

        val safeArgs: FinQuizArgs by navArgs()
        val catid = safeArgs.catid

        binding = FragmentFinQuizBinding.inflate(inflater, container, false)

        when (catid) {
            0 -> binding.image.setImageResource(R.drawable.science)
            1 -> binding.image.setImageResource(R.drawable.physique)
            2 -> binding.image.setImageResource(R.drawable.chimie)
            3 -> binding.image.setImageResource(R.drawable.biologie)
            4 -> binding.image.setImageResource(R.drawable.astronomie)
            5 -> binding.image.setImageResource(R.drawable.botanique)
            6 -> binding.image.setImageResource(R.drawable.geologie)
        }

        binding.contentFinQuiz.apply {
            lifecycleOwner = viewLifecycleOwner
            roomViewModel = viewModel
            finQuiz = this@FinQuiz
        }

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        NavigationUI.setupWithNavController(
            binding.collapsingToolbarLayout,
            binding.toolBar,
            navController,
            appBarConfiguration
        )

        binding.contentFinQuiz.classement.setOnClickListener {
            viewModel.username.observe(viewLifecycleOwner) {
                if ((it != null) && (viewModel.networkAccessOk)) {
                    val action =
                        FinQuizDirections.actionFinQuizToResult(
                            it,
                            viewModel.catId
                        )
                    findNavController().navigate(action)
                } else {
                    Snackbar.make(
                        binding.contentFinQuiz.classement,
                        getString(R.string.notavailable),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.contentFinQuiz.Rejouer.setOnClickListener {
            val action = FinQuizDirections.actionFinQuizToWelcomeFragment()
            findNavController().navigate(action)
        }

        binding.contentFinQuiz.Quitter.setOnClickListener {
            requireActivity().finish()
        }

        return binding.root
    }

    fun txtScore(score:Int):String{
        return getString(R.string.score2,score)
    }

}