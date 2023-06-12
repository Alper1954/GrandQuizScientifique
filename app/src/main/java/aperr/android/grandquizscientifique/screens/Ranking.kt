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
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.databinding.FragmentRankingBinding

class Ranking : Fragment() {


    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Log.i ("alain", "Ranking onCreateView")

        val binding = FragmentRankingBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            roomViewModel = viewModel
        }
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        viewModel.initListCatUser()
        val callback = { pos: Int -> selectCat(pos) }
        viewModel.statusData.observe(viewLifecycleOwner){
            if(it == 2){
                //("alain", "ListCatUser size: " + viewModel.listCatUser.size)
                val itemAdapter = ItemAdapter(viewModel.listCatUser, callback)
                binding.listCat.layoutManager = GridLayoutManager(activity, 3)
                binding.listCat.adapter = itemAdapter
            }
            binding.statusList.text = when(it){
                0 -> getString(R.string.notavailable)
                1 -> getString(R.string.connecting)
                2 -> getString(R.string.choose_cat)
                else -> getString(R.string.error)
            }
        }
        return binding.root
    }

    private fun selectCat(pos: Int) {
        viewModel.username.observe(viewLifecycleOwner) {
            val action = RankingDirections.actionRankingToResult(
                it,
                viewModel.listCatUser[pos].id
            )
            findNavController().navigate(action)
        }
    }


}