package sugtao4423.lod.ui.showimage.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import sugtao4423.lod.databinding.FragmentShowImageBinding

class ShowImageFragment : Fragment() {

    companion object {
        const val BUNDLE_KEY_URL = "url"
    }

    private val viewModel: ShowImageFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentShowImageBinding.inflate(inflater, container, false).also {
            it.viewModel = viewModel
        }
        viewModel.imageUrl = requireArguments().getString(BUNDLE_KEY_URL)!!
        return binding.root
    }

}
