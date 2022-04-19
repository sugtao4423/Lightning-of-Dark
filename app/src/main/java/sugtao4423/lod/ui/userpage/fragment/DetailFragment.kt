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
import sugtao4423.lod.ChromeIntent
import sugtao4423.lod.databinding.FragmentUserDetailBinding
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity
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
            it.viewModel = viewModel
        }

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
            val image = Intent(context, ImageFragmentActivity::class.java).apply {
                putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, arrayOf(it))
                putExtra(
                    ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE,
                    ImageFragmentActivity.TYPE_ICON
                )
            }
            startActivity(image)
        }
        viewModel.onStartBannerImageUrl.observe(viewLifecycleOwner) {
            val image = Intent(context, ImageFragmentActivity::class.java).apply {
                putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, arrayOf(it))
                putExtra(
                    ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE,
                    ImageFragmentActivity.TYPE_BANNER
                )
            }
            startActivity(image)
        }
        viewModel.onStartChromeUrl.observe(viewLifecycleOwner) {
            ChromeIntent(requireContext(), Uri.parse(it))
        }

        return binding.root
    }
}
