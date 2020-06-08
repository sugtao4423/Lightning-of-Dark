package sugtao4423.lod.userpage_fragment

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.loopj.android.image.SmartImageView
import sugtao4423.lod.App
import sugtao4423.lod.ChromeIntent
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity
import sugtao4423.lod.utils.Regex
import twitter4j.Relationship
import twitter4j.TwitterException
import twitter4j.URLEntity
import twitter4j.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class _0_detail : Fragment() {

    private lateinit var app: App
    var targetUser: User? = null

    private lateinit var isFollowIcon: TextView
    private lateinit var userBio: TextView
    private lateinit var userLocation: TextView
    private lateinit var userLink: TextView
    private lateinit var userTweetC: TextView
    private lateinit var userFavoriteC: TextView
    private lateinit var userFollowC: TextView
    private lateinit var userFollowerC: TextView
    private lateinit var userCreate: TextView
    private lateinit var sourceIcon: SmartImageView
    private lateinit var targetIcon: SmartImageView

    private lateinit var userBanner: SmartImageView
    private lateinit var userIcon: SmartImageView
    private lateinit var userName: TextView
    private lateinit var userScreenName: TextView
    private lateinit var protect: TextView

    private var isTextSet = false
    private var isPrepared = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.user_0, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = activity!!.applicationContext as App

        userBanner = view.findViewById(R.id.userBanner)
        userIcon = view.findViewById(R.id.userIcon)
        userName = view.findViewById(R.id.userName)
        userScreenName = view.findViewById(R.id.userScreenName)
        protect = view.findViewById(R.id.userPageProtected)

        userBio = view.findViewById(R.id.userBio)
        userLocation = view.findViewById(R.id.userLocation)
        userLink = view.findViewById(R.id.userLink)
        userTweetC = view.findViewById(R.id.userTweetCount)
        userFavoriteC = view.findViewById(R.id.userFavoriteCount)
        userFollowC = view.findViewById(R.id.userFollowCount)
        userFollowerC = view.findViewById(R.id.userFollowerCount)
        userCreate = view.findViewById(R.id.userCreateDate)
        sourceIcon = view.findViewById(R.id.userPageSourceIcon)
        targetIcon = view.findViewById(R.id.userPageTargetIcon)
        isFollowIcon = view.findViewById(R.id.userPageIsFollow)

        protect.visibility = View.GONE

        val tf = app.getFontAwesomeTypeface()
        protect.typeface = tf
        (view.findViewById<TextView>(R.id.iconTweetCount)).typeface = tf
        (view.findViewById<TextView>(R.id.iconFavoriteCount)).typeface = tf
        (view.findViewById<TextView>(R.id.iconFollowCount)).typeface = tf
        (view.findViewById<TextView>(R.id.iconFollowerCount)).typeface = tf
        (view.findViewById<TextView>(R.id.iconCreateDate)).typeface = tf

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
                protect.visibility = View.VISIBLE
            }
            userIcon.setImageUrl(it.originalProfileImageURLHttps, null, R.drawable.icon_loading)
            userBanner.setImageUrl(it.profileBannerRetinaURL)
            userName.text = it.name
            userScreenName.text = "@${it.screenName}"

            if (app.getCurrentAccount().screenName == it.screenName) {
                sourceIcon.visibility = View.GONE
                targetIcon.visibility = View.GONE
                isFollowIcon.visibility = View.GONE
            } else {
                sourceIcon.visibility = View.VISIBLE
                targetIcon.visibility = View.VISIBLE
                isFollowIcon.visibility = View.VISIBLE
                followCheck()
                setSourceAndTargetIcon()
            }

            setLinkTouch(userBio, replaceUrlEntity2ExUrl(it.description, it.descriptionURLEntities))
            setLinkTouch(userLocation, it.location)
            setLinkTouch(userLink, replaceUrlEntity2ExUrl(it.url, it.urlEntity))
            userTweetC.text = numberFormat(it.statusesCount)
            userFavoriteC.text = numberFormat(it.favouritesCount)
            userFollowC.text = numberFormat(it.friendsCount)
            userFollowerC.text = numberFormat(it.followersCount)
            userCreate.text = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(it.createdAt)
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
        object : AsyncTask<Unit, Unit, Relationship?>() {

            override fun doInBackground(vararg params: Unit?): Relationship? {
                return try {
                    app.getTwitter().showFriendship(app.getCurrentAccount().screenName, targetUser!!.screenName)
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: Relationship?) {
                if (result != null) {
                    isFollowIcon.typeface = app.getFontAwesomeTypeface()
                    isFollowIcon.text = when {
                        result.isSourceFollowingTarget && result.isSourceFollowedByTarget -> getString(R.string.icon_followEach)
                        result.isSourceFollowingTarget -> getString(R.string.icon_followFollow)
                        result.isSourceFollowedByTarget -> getString(R.string.icon_followFollower)
                        result.isSourceBlockingTarget -> getString(R.string.icon_followBlock)
                        else -> ""
                    }
                }
            }
        }.execute()
    }

    private fun setSourceAndTargetIcon() {
        object : AsyncTask<Unit, Unit, Pair<String, String>?>() {

            override fun doInBackground(vararg params: Unit?): Pair<String, String>? {
                return try {
                    Pair(
                            app.getTwitter().verifyCredentials().biggerProfileImageURLHttps,
                            targetUser!!.biggerProfileImageURLHttps
                    )
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: Pair<String, String>?) {
                if (result != null) {
                    sourceIcon.setImageUrl(result.first, null, R.drawable.icon_loading)
                    targetIcon.setImageUrl(result.second, null, R.drawable.icon_loading)
                } else {
                    ShowToast(activity!!.applicationContext, R.string.error_get_user_icon)
                }
            }
        }.execute()
    }

    private fun setClick() {
        val context = activity!!

        userIcon.setOnClickListener {
            val image = Intent(context, ImageFragmentActivity::class.java).apply {
                putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, arrayOf(targetUser!!.originalProfileImageURLHttps))
                putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE, ImageFragmentActivity.TYPE_ICON)
            }
            context.startActivity(image)
        }
        userIcon.setOnLongClickListener {
            ChromeIntent(context, Uri.parse(targetUser!!.originalProfileImageURLHttps))
            true
        }

        userBanner.setOnClickListener {
            if (targetUser!!.profileBannerURL != null) {
                val image = Intent(context, ImageFragmentActivity::class.java).apply {
                    putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, arrayOf(targetUser!!.profileBanner1500x500URL))
                    putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_TYPE, ImageFragmentActivity.TYPE_BANNER)
                }
                context.startActivity(image)
            }
        }
        userBanner.setOnLongClickListener {
            ChromeIntent(context, Uri.parse(targetUser!!.originalProfileImageURLHttps))
            true
        }
        userBanner.setOnLongClickListener {
            if (targetUser!!.profileBannerURL != null) {
                ChromeIntent(context, Uri.parse(targetUser!!.profileBanner1500x500URL))
            }
            true
        }
    }

}