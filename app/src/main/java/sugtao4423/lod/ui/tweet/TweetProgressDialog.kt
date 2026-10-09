package sugtao4423.lod.ui.tweet

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.R
import sugtao4423.lod.databinding.DialogNewtweetProgressBinding

class TweetProgressDialog : DialogFragment() {

    private val viewModel: TweetActivityViewModel by activityViewModels()
    private var binding: DialogNewtweetProgressBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = DialogNewtweetProgressBinding.inflate(layoutInflater)
        binding = b

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tweetProgress.collect {
                    @SuppressLint("SetTextI18n")
                    b.percentText.text = "$it%"
                    b.progressBar.progress = it
                    if (it >= 100) {
                        dismiss()
                    }
                }
            }
        }

        isCancelable = false
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.tweeting)
            .setView(b.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
