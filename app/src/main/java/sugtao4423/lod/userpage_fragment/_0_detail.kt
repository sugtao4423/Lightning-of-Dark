package sugtao4423.lod.userpage_fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.App
import sugtao4423.lod.ChromeIntent
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.databinding.User0Binding
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity
import sugtao4423.lod.utils.Regex
import twitter4j.TwitterException
import twitter4j.URLEntity
import twitter4j.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class _0_detail : Fragment() {

    private lateinit var binding: User0Binding
    private lateinit var app: App
    var targetUser: User? = null

    private var isTextSet = false
    private var isPrepared = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = User0Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as App
        binding.userPageProtected.visibility = View.GONE

        app.getFontAwesomeTypeface().let {
            binding.userPageProtected.typeface = it
            binding.iconTweetCount.typeface = it
            binding.iconFavoriteCount.typeface = it
            binding.iconFollowCount.typeface = it
            binding.iconFollowerCount.typeface = it
            binding.iconCreateDate.typeface = it
        }

        setClick()
        isPrepared = true
        setText()
    }

    fun setText() {
        if (targetUser == null || isTextSet || !isPrepared) {
            return
        }
        isTextSet = true

        targetUser!!.also {
            if (it.isProtected) {
                binding.userPageProtected.visibility = View.VISIBLE
            }
            Glide.with(this).load(it.originalProfileImageURLHttps).placeholder(R.drawable.icon_loading).into(binding.userIcon)
            if (!it.profileBannerRetinaURL.isNullOrEmpty()) {
                Glide.with(this).load(it.profileBannerRetinaURL).placeholder(R.drawable.user_header_empty).into(binding.userBanner)
            }
            binding.userName.text = it.name
            binding.userScreenName.text = "@${it.screenName}"

            if (app.account.screenName == it.screenName) {
                binding.userPageSourceIcon.visibility = View.GONE
                binding.userPageTargetIcon.visibility = View.GONE
                binding.userPageIsFollow.visibility = View.GONE
            } else {
                binding.userPageSourceIcon.visibility = View.VISIBLE
                binding.userPageTargetIcon.visibility = View.VISIBLE
                binding.userPageIsFollow.visibility = View.VISIBLE
                followCheck()
                setSourceAndTargetIcon()
            }

            setLinkTouch(binding.userBio, replaceUrlEntity2ExUrl(it.description, it.descriptionURLEntities))
            setLinkTouch(binding.userLocation, it.location)
            setLinkTouch(binding.userLink, replaceUrlEntity2ExUrl(it.url, it.urlEntity))
            binding.userTweetCount.text = numberFormat(it.statusesCount)
            binding.userFavoriteCount.text = numberFormat(it.favouritesCount)
            binding.userFollowCount.text = numberFormat(it.friendsCount)
            binding.userFollowerCount.text = numberFormat(it.followersCount)
            binding.userCreateDate.text = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(it.createdAt)
        }
    }

    private fun replaceUrlEntity2ExUrl(target: String?, entity: URLEntity): String {
        return target?.replace(entity.url, entity.expandedURL) ?: ""
    }

    private fun replaceUrlEntity2ExUrl(target: String?, entity: Array<URLEntity>): String {
        var result = target ?: ""
        entity.map {
            result = replaceUrlEntity2ExUrl(target, it)
        }
        return result
    }

    private fun setLinkTouch(view: TextView, setStr: String) {
        if (setStr.isEmpty()) {
            view.text = ""
            return
        }
        val ss = SpannableString(setStr)
        val m = Regex.userAndAnyUrl.matcher(setStr)
        while (m.find()) {
            val t = m.group()
            if (t.startsWith("@") || t.startsWith("http")) {
                ss.setSpan(object : URLSpan(t) {
                    override fun onClick(widget: View) {
                        if (t.startsWith("@")) {
                            val intent = Intent(context, UserPage::class.java)
                            intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, this.url.replace("@", ""))
                            activity!!.startActivity(intent)
                        } else if (t.startsWith("http")) {
                            ChromeIntent(activity!!, Uri.parse(t))
                        }
                    }
                }, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                ss.setSpan(URLSpan(t), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        view.text = ss
        view.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun numberFormat(num: Int): String {
        return NumberFormat.getInstance().format(num)
    }

    private fun followCheck() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    app.twitter.showFriendship(app.account.screenName, targetUser!!.screenName)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                binding.userPageIsFollow.typeface = app.getFontAwesomeTypeface()
                binding.userPageIsFollow.text = when {
                    result.isSourceFollowingTarget && result.isSourceFollowedByTarget -> getString(R.string.icon_followEach)
                    result.isSourceFollowingTarget -> getString(R.string.icon_followFollow)
                    result.isSourceFollowedByTarget -> getString(R.string.icon_followFollower)
                    result.isSourceBlockingTarget -> getString(R.string.icon_followBlock)
                    else -> ""
                }
            }
        }
    }

    private fun setSourceAndTargetIcon() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    Pair(app.twitter.verifyCredentials().biggerProfileImageURLHttps,
                            targetUser!!.biggerProfileImageURLHttps)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                Glide.with(this@_0_detail).load(result.first).placeholder(R.drawable.icon_loading).into(binding.userPageSourceIcon)
                Glide.with(this@_0_detail).load(result.second).placeholder(R.drawable.icon_loading).into(binding.userPageTargetIcon)
            } else {
                ShowToast(requireContext().applicationContext, R.string.error_get_user_icon)
            }
        }
    }

    private fun setClick() {
        val context = requireContext()

        binding.userIcon.setOnClickListener {
            val image = Intent(context, ImageFragmentActivity::class.java).apply {
                putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, arrayOf(targetUser!!.originalProfileImageURLHttps))
                putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE, ImageFragmentActivity.TYPE_ICON)
            }
            context.startActivity(image)
        }
        binding.userIcon.setOnLongClickListener {
            ChromeIntent(context, Uri.parse(targetUser!!.originalProfileImageURLHttps))
            true
        }

        binding.userBanner.setOnClickListener {
            if (targetUser!!.profileBannerURL != null) {
                val image = Intent(context, ImageFragmentActivity::class.java).apply {
                    putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, arrayOf(targetUser!!.profileBanner1500x500URL))
                    putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE, ImageFragmentActivity.TYPE_BANNER)
                }
                context.startActivity(image)
            }
        }
        binding.userBanner.setOnLongClickListener {
            if (targetUser!!.profileBannerURL != null) {
                ChromeIntent(context, Uri.parse(targetUser!!.profileBanner1500x500URL))
            }
            true
        }
    }

}
