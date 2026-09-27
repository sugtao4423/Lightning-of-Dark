package sugtao4423.lod.ui.showimage.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.databinding.FragmentShowImageBinding
import sugtao4423.lod.ui.loadUrl

class ShowImageFragment : Fragment() {

    companion object {
        const val BUNDLE_KEY_URL = "url"
    }

    private val viewModel: ShowImageFragmentViewModel by viewModels()
    private lateinit var binding: FragmentShowImageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }

        val imageUrl = requireArguments().getString(BUNDLE_KEY_URL)!!
        binding.imageView.loadUrl(imageUrl, viewModel.requestListener)
    }

    private fun render(state: ShowImageUiState) {
        binding.progressBar.visibility = if (state.isShowProgressBar) View.VISIBLE else View.GONE
    }

}
