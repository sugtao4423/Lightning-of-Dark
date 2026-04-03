package sugtao4423.lod.ui.userpage.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import sugtao4423.lod.R
import sugtao4423.lod.databinding.FragmentUserDetailBinding
import sugtao4423.lod.ui.loadUrl
import sugtao4423.lod.ui.setLodLinkMovementString
import sugtao4423.lod.ui.showimage.ShowImageActivity
import sugtao4423.lod.ui.userpage.UserPageActivityViewModel
import sugtao4423.lod.ui.userpage.converter.UserConverter
import sugtao4423.lod.utils.ChromeIntent
import twitter4j.User

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
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPageViewModel.user.observe(viewLifecycleOwner) {
            bindUser(it)
            viewModel.checkRelationShip(it)
            viewModel.getRelationshipIconUrls(it)
        }

        viewModel.relationshipIcon.observe(viewLifecycleOwner) {
            binding.relationshipText.text = it
        }
        viewModel.relationShipIconUrls.observe(viewLifecycleOwner) {
            binding.relationshipMeIcon.loadUrl(it.me)
            binding.relationshipTargetIcon.loadUrl(it.target)
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

    private fun bindUser(user: User?) {
        binding.apply {
            bannerImage.loadUrl(
                UserConverter.bannerUrl(user),
                ContextCompat.getDrawable(requireContext(), R.drawable.user_header_empty)
            )
            bannerImage.setOnClickListener { viewModel.onClickBanner(user) }
            bannerImage.setOnLongClickListener { viewModel.onLongClickBanner(user) }

            iconImage.loadUrl(UserConverter.iconUrl(user))
            iconImage.setOnClickListener { viewModel.onClickIcon(user) }
            iconImage.setOnLongClickListener { viewModel.onLongClickIcon(user) }

            userName.text = UserConverter.name(user)
            protectIcon.visibility = if (user?.isProtected == true) View.VISIBLE else View.GONE
            screenName.text = UserConverter.screenName(user)

            relationshipLayout.visibility =
                if (viewModel.isShowRelationship(user)) View.VISIBLE else View.GONE

            bioText.setLodLinkMovementString(UserConverter.bio(user))
            locationText.setLodLinkMovementString(UserConverter.location(user))
            linkText.setLodLinkMovementString(UserConverter.link(user))

            tweetCount.text = UserConverter.tweetCount(user)
            favCount.text = UserConverter.favoriteCount(user)
            followCount.text = UserConverter.followCount(user)
            followerCount.text = UserConverter.followerCount(user)
            createDate.text = UserConverter.createDate(user)
        }
    }

}
