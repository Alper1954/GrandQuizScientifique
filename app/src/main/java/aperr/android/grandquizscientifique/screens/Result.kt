package aperr.android.grandquizscientifique.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import aperr.android.grandquizscientifique.databinding.FragmentResultBinding
import java.util.Locale


class Result : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val safeArgs: ResultArgs by navArgs()
        val player = safeArgs.player
        val posCat = safeArgs.posCat

        val lang = if(Locale.getDefault().language == "fr") 0 else 1

        //Log.i("alain", "player: $player\n")
        //Log.i("alain", "posCat: $posCat\n")

        val binding = FragmentResultBinding.inflate(inflater, container, false)

        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        val url = "http://aperrault.atspace.cc/quizScience.php?pseudo=$player&cat=$posCat&lang=$lang"
        binding.webView.loadUrl(url)

        return binding.root
    }


}