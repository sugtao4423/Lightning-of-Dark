package sugtao4423.lod.ui.userpage.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import sugtao4423.lod.R
import sugtao4423.lod.databinding.FragmentUserDetailBinding
import sugtao4423.lod.ui.loadUrl
import sugtao4423.lod.ui.setLodLinkMovementString
import sugtao4423.lod.ui.showimage.ShowImageActivity
import sugtao4423.lod.ui.userpage.UserPageActivityViewModel
import sugtao4423.lod.utils.ChromeIntent

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentUserDetailBinding

    private val userPageViewModel: UserPageActivityViewModel by activityViewModels()
    private val viewModel: DetailFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.apply {
            val fontAwesome = viewModel.fontAwesomeTypeface()
            protectIcon.typeface = fontAwesome
            relationshipText.typeface = fontAwesome
            tweetCountIcon.typeface = fontAwesome
            favCountIcon.typeface = fontAwesome
            followCountIcon.typeface = fontAwesome
            followerCountIcon.typeface = fontAwesome
            createDateIcon.typeface = fontAwesome

            bannerImage.setOnClickListener { viewModel.onClickBanner() }
            bannerImage.setOnLongClickListener { viewModel.onLongClickBanner() }

            iconImage.setOnClickListener { viewModel.onClickIcon() }
            iconImage.setOnLongClickListener { viewModel.onLongClickIcon() }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { userPageViewModel.uiState.collect { it.user?.let(viewModel::setUser) } }
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    private fun render(state: DetailUiState) = binding.apply {
        bannerImage.loadUrl(
            state.bannerUrl,
            ContextCompat.getDrawable(requireContext(), R.drawable.user_header_empty)
        )
        iconImage.loadUrl(state.iconUrl)
        userName.text = state.name
        screenName.text = state.screenName
        protectIcon.visibility = if (state.isShowProtected) View.VISIBLE else View.GONE

        relationshipLayout.visibility = if (state.isShowRelationship) View.VISIBLE else View.GONE
        if (state.isShowRelationship) {
            relationshipText.text = state.relationshipIcon
            relationshipMeIcon.loadUrl(state.myIconUrl)
            relationshipTargetIcon.loadUrl(state.iconUrl)
        }

        bioText.setLodLinkMovementString(state.bio)
        locationText.setLodLinkMovementString(state.location)
        linkText.setLodLinkMovementString(state.link)

        tweetCount.text = state.tweetCount
        favCount.text = state.favoriteCount
        followCount.text = state.followCount
        followerCount.text = state.followerCount
        createDate.text = state.createDate
    }

    private fun handleEvent(event: DetailEvent) = when (event) {
        is DetailEvent.StartBannerImage -> {
            val image = Intent(context, ShowImageActivity::class.java).apply {
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_URLS, arrayOf(event.url))
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_TYPE, ShowImageActivity.TYPE_BANNER)
            }
            startActivity(image)
        }

        is DetailEvent.StartIconImage -> {
            val image = Intent(context, ShowImageActivity::class.java).apply {
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_URLS, arrayOf(event.url))
                putExtra(ShowImageActivity.INTENT_EXTRA_KEY_TYPE, ShowImageActivity.TYPE_ICON)
            }
            startActivity(image)
        }

        is DetailEvent.StartChrome -> ChromeIntent(requireContext(), event.url.toUri())
    }

}
