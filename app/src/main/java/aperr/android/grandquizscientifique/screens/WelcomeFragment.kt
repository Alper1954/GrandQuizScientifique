package aperr.android.grandquizscientifique.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.GridLayoutManager
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.databinding.FragmentWelcomeBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Log.i("alain", "WelcomeFragment onCreateView")

        binding = FragmentWelcomeBinding.inflate(inflater, container, false)


        val navController = NavHostFragment.findNavController(this)
        //val appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.welcomeFragment), binding.drawerLayout)

        NavigationUI.setupWithNavController(binding.toolBar, navController, appBarConfiguration)
        //binding.toolBar.setupWithNavController(navController, binding.drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)

        viewModel.username.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.toolBar.inflateMenu(R.menu.menu_toolbar)
            }
        }


        binding.toolBar.setOnMenuItemClickListener {
            it.onNavDestinationSelected(navController)
        }

        val callback = { pos: Int -> selectCat(pos) }
        binding.contentWelcome.listCat.layoutManager = GridLayoutManager(activity, 3)
        binding.contentWelcome.listCat.adapter = ItemAdapter(viewModel.listCat, callback)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.emptyListId.collect {
                        Snackbar.make(binding.contentWelcome.chooseCat,getString(R.string.catNotAvailable),Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goodListId.collect {
                        val action =
                            WelcomeFragmentDirections.actionWelcomeFragmentToGameFragment(it.id, it.name)
                        findNavController().navigate(action)
                }
            }
        }

        return binding.root
    }


    private fun selectCat(pos: Int) {
        viewModel.selectCat(pos)
    }

}