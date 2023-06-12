package aperr.android.grandquizscientifique.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.databinding.FragmentExplicationBinding


class ExplicationFragment : Fragment() {
    private lateinit var binding: FragmentExplicationBinding

    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("alain", "ExplicationFragment onCreateView")

        val safeArgs:ExplicationFragmentArgs by navArgs()
        val catid = safeArgs.catid

        binding = FragmentExplicationBinding.inflate(inflater, container, false)

        when (catid) {
            0 -> binding.image.setImageResource(R.drawable.science)
            1 -> binding.image.setImageResource(R.drawable.physique)
            2 -> binding.image.setImageResource(R.drawable.chimie)
            3 -> binding.image.setImageResource(R.drawable.biologie)
            4 -> binding.image.setImageResource(R.drawable.astronomie)
            5 -> binding.image.setImageResource(R.drawable.botanique)
            6 -> binding.image.setImageResource(R.drawable.geologie)
        }

        binding.contentExplication.apply {
            lifecycleOwner = viewLifecycleOwner
            roomViewModel = viewModel
        }

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        NavigationUI.setupWithNavController(binding.collapsingToolbarLayout,binding.toolBar, navController, appBarConfiguration)

        return binding.root
    }

    override fun onDestroy() {
        Log.i("alain", "ExplicationFragment Destroy")
        super.onDestroy()
    }

}