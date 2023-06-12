package aperr.android.grandquizscientifique.screens

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import aperr.android.grandquizscientifique.R

class Dialplayer : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.noplayer)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return alertDialog
    }
}