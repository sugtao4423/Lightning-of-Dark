package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(private val json: JSONObject) : Comparable<User>, Serializable {

    val id: Long
    val name: String
    val screenName: String
    val location: String
    val description: String
    val url: String
    val protected: Boolean

    val descriptionUrlEntities: List<URLEntity>
    val urlEntities: List<URLEntity>

    val isFollowing: Boolean
    val isFollowedBy: Boolean

    val statusesCount: Int
    val favouritesCount: Int
    val friendsCount: Int
    val followersCount: Int
    val listedCount: Int
    val mediaCount: Int

    val pinnedStatusIds: List<Long>

    val profileImageNormalUrl: String
    val profileImageBiggerUrl: String
    val profileImageMiniUrl: String
    val profileImage400x400Url: String

    val profileBannerUrl: String?
    val profileBannerRetinaUrl: String?
    val profileBannerIpadUrl: String?
    val profileBannerIpadRetinaUrl: String?
    val profileBannerMobileUrl: String?
    val profileBannerMobileRetinaUrl: String?
    val profileBanner300x100Url: String?
    val profileBanner600x200Url: String?
    val profileBanner1500x500Url: String?

    val createdAt: Date

    init {
        id = json.getString("rest_id").toLong()

        val legacy = json.getJSONObject("legacy")
        name = legacy.getString("name")
        screenName = legacy.getString("screen_name")
        location = legacy.getString("location")
        description = legacy.getString("description")
        url = legacy.optString("url", "")
        protected = legacy.optBoolean("protected", false)

        val entities = legacy.getJSONObject("entities")
        descriptionUrlEntities = entities.getJSONObject("description").getJSONArray("urls").let {
            (0 until it.length()).map { i -> URLEntity(it.getJSONObject(i)) }
        }
        urlEntities = entities.optJSONObject("url")?.getJSONArray("urls")?.let {
            (0 until it.length()).map { i -> URLEntity(it.getJSONObject(i)) }
        } ?: emptyList()

        isFollowing = legacy.optBoolean("following", false)
        isFollowedBy = legacy.optBoolean("followed_by", false)

        statusesCount = legacy.getInt("statuses_count")
        favouritesCount = legacy.getInt("favourites_count")
        friendsCount = legacy.getInt("friends_count")
        followersCount = legacy.getInt("followers_count")
        listedCount = legacy.getInt("listed_count")
        mediaCount = legacy.getInt("media_count")

        pinnedStatusIds = legacy.getJSONArray("pinned_tweet_ids_str").let {
            (0 until it.length()).map { i -> it.getString(i).toLong() }
        }

        profileImageNormalUrl = legacy.getString("profile_image_url_https")
        profileImageBiggerUrl = profileImageNormalUrl.replace("_normal", "_bigger")
        profileImageMiniUrl = profileImageNormalUrl.replace("_normal", "_mini")
        profileImage400x400Url = profileImageNormalUrl.replace("_normal", "_400x400")

        val banner = legacy.optString("profile_banner_url", "").ifEmpty { null }
        profileBannerUrl = banner?.let { "${it}/web" }
        profileBannerRetinaUrl = banner?.let { "${it}/web_retina" }
        profileBannerIpadUrl = banner?.let { "${it}/ipad" }
        profileBannerIpadRetinaUrl = banner?.let { "${it}/ipad_retina" }
        profileBannerMobileUrl = banner?.let { "${it}/mobile" }
        profileBannerMobileRetinaUrl = banner?.let { "${it}/mobile_retina" }
        profileBanner300x100Url = banner?.let { "${it}/300x100" }
        profileBanner600x200Url = banner?.let { "${it}/600x200" }
        profileBanner1500x500Url = banner?.let { "${it}/1500x500" }

        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US)
        createdAt = dateFormat.parse(legacy.getString("created_at"))!!
    }

    override fun compareTo(other: User): Int {
        return (id - other.id).toInt()
    }

}
