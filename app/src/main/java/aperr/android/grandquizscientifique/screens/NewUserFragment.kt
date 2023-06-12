package aperr.android.grandquizscientifique.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import aperr.android.grandquizscientifique.QuizApplication
import aperr.android.grandquizscientifique.R
import aperr.android.grandquizscientifique.databinding.FragmentNewUserBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class NewUserFragment : Fragment() {

    private lateinit var binding: FragmentNewUserBinding

    private val viewModel: RoomViewModel by activityViewModels {
        RoomViewModelFactory(
            activity?.application as QuizApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewUserBinding.inflate(inflater, container, false)

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.next.visibility=View.INVISIBLE


        binding.nameEdit.doOnTextChanged { _, _, _, _ ->
            val userName:String = binding.nameEdit.text.toString()
            binding.valid.isEnabled = userName.isNotEmpty()
        }

        binding.valid.setOnClickListener {
            val userName:String = binding.nameEdit.text.toString()
            //Log.i("alain", userName)
            viewModel.newPlayer(userName, requireContext() )
        }

        binding.next.setOnClickListener {
            val action = NewUserFragmentDirections.actionNewUserFragmentToWelcomeFragment()
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.checkingInput.collect {
                    if(it){
                        binding.name.helperText=getString(R.string.checkingName)
                    }else{
                        binding.name.helperText= null
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resultValidInput.collect {
                    when(it){
                        "0" -> {
                            val action = NewUserFragmentDirections.actionNewUserFragmentToWelcomeFragment()
                            findNavController().navigate(action)
                        }
                        "1" -> {
                            binding.name.error=getString(R.string.badName)
                            Snackbar.make(binding.valid,getString(R.string.newName),Snackbar.LENGTH_SHORT).show()
                        }
                        "2" -> {
                            binding.name.error=getString(R.string.saveError)
                            binding.valid.visibility=View.INVISIBLE
                            binding.next.visibility=View.VISIBLE
                        }
                    }
                }
            }
        }

        return binding.root
    }
}