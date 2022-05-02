package sugtao4423.lod.ui.userpage.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import sugtao4423.lod.utils.ChromeIntent
import sugtao4423.lod.databinding.FragmentUserDetailBinding
import sugtao4423.lod.ui.showimage.ShowImageActivity
import sugtao4423.lod.ui.userpage.UserPageActivityViewModel

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentUserDetailBinding

    private val userPageViewModel: UserPageActivityViewModel by activityViewModels()
    private val viewModel: DetailFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPageViewModel.user.observe(viewLifecycleOwner) {
            binding.user = it
            viewModel.checkRelationShip(it)
            viewModel.getRelationshipIconUrls(it)
        }

        viewModel.relationshipIcon.observe(viewLifecycleOwner) {
            binding.relationshipText = it
        }
        viewModel.relationShipIconUrls.observe(viewLifecycleOwner) {
            binding.relationshipIcons = it
        }
        viewModel.onStartIconImageUrl.observe(viewLifecycleOwner) {
            val image = Intent(context, ShowImageActivity::class.java).apply {
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_URLS, arrayOf(it))
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_TYPE, ShowImageActivity.TYPE_ICON)
            }
            startActivity(image)
        }
        viewModel.onStartBannerImageUrl.observe(viewLifecycleOwner) {
            val image = Intent(context, ShowImageActivity::class.java).apply {
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_URLS, arrayOf(it))
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_TYPE, ShowImageActivity.TYPE_BANNER)
            }
            startActivity(image)
        }
        viewModel.onStartChromeUrl.observe(viewLifecycleOwner) {
            ChromeIntent(requireContext(), Uri.parse(it))
        }
    }
}
