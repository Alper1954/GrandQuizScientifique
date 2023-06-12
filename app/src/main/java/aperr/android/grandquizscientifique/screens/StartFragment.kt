package aperr.android.grandquizscientifique.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.databinding.FragmentStartBinding


class StartFragment : Fragment() {

    private lateinit var binding: FragmentStartBinding

    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Log.i("alain", "StartFragment onCreateView")

        binding = FragmentStartBinding.inflate(inflater, container, false)

        binding.contentStart.apply {
            lifecycleOwner = viewLifecycleOwner
            roomViewModel = viewModel
        }

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        NavigationUI.setupWithNavController(
            binding.collapsingToolbarLayout,
            binding.toolBar,
            navController,
            appBarConfiguration
        )

        binding.contentStart.playId.setOnClickListener {
            viewModel.initNetorkAccess()
            viewModel.username.observe(viewLifecycleOwner) {
                if (it == null) {
                    val action = StartFragmentDirections.actionStartFragmentToNewUserFragment()
                    findNavController().navigate(action)
                } else {
                    val action = StartFragmentDirections.actionStartFragmentToWelcomeFragment()
                    findNavController().navigate(action)
                }
            }
        }

        return binding.root
    }



}