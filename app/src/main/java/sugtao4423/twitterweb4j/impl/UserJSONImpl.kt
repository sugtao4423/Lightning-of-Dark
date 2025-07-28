package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.falseBoolean
import sugtao4423.twitterweb4j.nullString
import twitter4j.JSONArray
import twitter4j.JSONObject
import twitter4j.RateLimitStatus
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.URLEntity
import twitter4j.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserJSONImpl(@Transient private val json: JSONObject) : User, java.io.Serializable {

    @Transient
    private val avatar = json.optJSONObject("avatar") ?: null

    @Transient
    private val core = json.optJSONObject("core") ?: null

    @Transient
    private val legacy = json.getJSONObject("legacy")

    @Transient
    private val loc = json.optJSONObject("location") ?: null

    @Transient
    private val privacy = json.optJSONObject("privacy") ?: null

    @Transient
    private val verification = json.optJSONObject("verification") ?: null

    private val id = json.getString("rest_id").toLong()

    private val name = when {
        legacy.has("name") -> legacy.getString("name")
        core != null && core.has("name") -> core.getString("name")
        else -> "null"
    }
    private val email = legacy.nullString("email")
    private val screenName = when {
        legacy.has("screen_name") -> legacy.getString("screen_name")
        core != null && core.has("screen_name") -> core.getString("screen_name")
        else -> "null"
    }
    private val location = when {
        legacy.has("location") -> legacy.getString("location")
        loc != null && loc.has("location") -> loc.getString("location")
        else -> "null"
    }

    private val descriptionURLEntities = getURLEntities("description")
    private val urlEntities = getURLEntities("url")
    private val description = legacy.getString("description")

    private val isContributorsEnabled = legacy.falseBoolean("contributors_enabled")
    private val profileImageUrlHttps = when {
        legacy.has("profile_image_url_https") -> legacy.getString("profile_image_url_https")
        avatar != null && avatar.has("image_url") -> avatar.getString("image_url")
        else -> throw TwitterException("Profile image URL not found")
    }
    private val isDefaultProfileImage = legacy.falseBoolean("default_profile_image")
    private val url = legacy.nullString("url")
    private val isProtected = when {
        legacy.has("protected") -> legacy.getBoolean("protected")
        privacy != null && privacy.has("protected") -> privacy.getBoolean("protected")
        else -> false
    }
    private val isGeoEnabled = legacy.falseBoolean("geo_enabled")
    private val isVerified = when {
        legacy.has("verified") -> legacy.getBoolean("verified")
        verification != null && verification.has("verified") -> verification.getBoolean("verified")
        else -> false
    }
    private val translator = legacy.falseBoolean("is_translator")
    private val followersCount = legacy.getInt("followers_count")

    private val profileBackgroundColor = legacy.nullString("profile_background_color")
    private val profileTextColor = legacy.nullString("profile_text_color")
    private val profileLinkColor = legacy.nullString("profile_link_color")
    private val profileSidebarFillColor = legacy.nullString("profile_sidebar_fill_color")
    private val profileSidebarBorderColor = legacy.nullString("profile_sidebar_border_color")
    private val profileUseBackgroundImage = legacy.falseBoolean("profile_use_background_image")
    private val isDefaultProfile = legacy.falseBoolean("default_profile")
    private val showAllInlineMedia = legacy.falseBoolean("show_all_inline_media")
    private val friendsCount = legacy.getInt("friends_count")
    private val createdAt = when {
        legacy.has("created_at") -> legacy.getString("created_at")
        core != null && core.has("created_at") -> core.getString("created_at")
        else -> throw TwitterException("Created at date not found")
    }.let { SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US).parse(it) }
    private val favouritesCount = legacy.getInt("favourites_count")
    private val utcOffset = legacy.optInt("utc_offset", 0)
    private val timeZone = legacy.nullString("time_zone")
    private val profileBackgroundImageUrlHttps =
        legacy.nullString("profile_background_image_url_https")
    private val profileBannerImageUrl = legacy.nullString("profile_banner_url")
    private val profileBackgroundTiled = legacy.falseBoolean("profile_background_tile")
    private val lang = legacy.nullString("lang")
    private val statusesCount = legacy.getInt("statuses_count")
    private val listedCount = legacy.getInt("listed_count")
    private val isFollowRequestSent = legacy.falseBoolean("follow_request_sent")

    private val withheldInCountries = json.optJSONArray("withheld_in_countries")?.let {
        (0 until it.length()).map { i -> it.getString(i) }.toTypedArray()
    } ?: emptyArray()

    private fun getURLEntities(category: String): Array<URLEntity> {
        val urls =
            json.optJSONObject("entities")?.optJSONArray(category) ?: json.optJSONObject("legacy")
                ?.optJSONObject("entities")?.optJSONObject(category)?.optJSONArray("urls")
            ?: JSONArray()

        return (0 until urls.length()).map {
            URLEntityJSONImpl(urls.getJSONObject(it))
        }.toTypedArray()
    }

    private fun toResizedURL(originalURL: String, sizeSuffix: String): String {
        val index = originalURL.lastIndexOf("_")
        val suffixIndex = originalURL.lastIndexOf(".")
        val slashIndex = originalURL.lastIndexOf("/")
        val url = originalURL.substring(0, index) + sizeSuffix
        return if (suffixIndex > slashIndex) {
            url + originalURL.substring(suffixIndex)
        } else {
            url
        }
    }

    override fun getId(): Long = id
    override fun getName(): String = name
    override fun getEmail(): String? = email
    override fun getScreenName(): String = screenName
    override fun getLocation(): String = location
    override fun getDescription(): String = description
    override fun isContributorsEnabled(): Boolean = isContributorsEnabled
    override fun getProfileImageURL(): String = profileImageUrlHttps
    override fun getBiggerProfileImageURL(): String = toResizedURL(profileImageUrlHttps, "_bigger")
    override fun getMiniProfileImageURL(): String = toResizedURL(profileImageUrlHttps, "_mini")
    override fun getOriginalProfileImageURL(): String = toResizedURL(profileImageUrlHttps, "")
    override fun get400x400ProfileImageURL(): String =
        toResizedURL(profileImageUrlHttps, "_400x400")

    override fun getProfileImageURLHttps(): String = getProfileImageURL()
    override fun getBiggerProfileImageURLHttps(): String = getBiggerProfileImageURL()
    override fun getMiniProfileImageURLHttps(): String = getMiniProfileImageURL()
    override fun getOriginalProfileImageURLHttps(): String = getOriginalProfileImageURL()
    override fun get400x400ProfileImageURLHttps(): String = get400x400ProfileImageURL()
    override fun isDefaultProfileImage(): Boolean = isDefaultProfileImage
    override fun getURL(): String? = url
    override fun isProtected(): Boolean = isProtected
    override fun getFollowersCount(): Int = followersCount
    override fun getStatus(): Status? = null
    override fun getProfileBackgroundColor(): String? = profileBackgroundColor
    override fun getProfileTextColor(): String? = profileTextColor
    override fun getProfileLinkColor(): String? = profileLinkColor
    override fun getProfileSidebarFillColor(): String? = profileSidebarFillColor
    override fun getProfileSidebarBorderColor(): String? = profileSidebarBorderColor
    override fun isProfileUseBackgroundImage(): Boolean = profileUseBackgroundImage
    override fun isDefaultProfile(): Boolean = isDefaultProfile
    override fun isShowAllInlineMedia(): Boolean = showAllInlineMedia
    override fun getFriendsCount(): Int = friendsCount
    override fun getCreatedAt(): Date = createdAt
    override fun getFavouritesCount(): Int = favouritesCount
    override fun getUtcOffset(): Int = utcOffset
    override fun getTimeZone(): String? = timeZone
    override fun getProfileBackgroundImageURL(): String? = profileBackgroundImageUrlHttps
    override fun getProfileBackgroundImageUrlHttps(): String? = getProfileBackgroundImageURL()

    override fun getProfileBannerURL(): String? = profileBannerImageUrl?.let { "$it/web" }
    override fun getProfileBannerRetinaURL(): String? =
        profileBannerImageUrl?.let { "$it/web_retina" }

    override fun getProfileBannerIPadURL(): String? = profileBannerImageUrl?.let { "$it/ipad" }
    override fun getProfileBannerIPadRetinaURL(): String? =
        profileBannerImageUrl?.let { "$it/ipad_retina" }

    override fun getProfileBannerMobileURL(): String? = profileBannerImageUrl?.let { "$it/mobile" }
    override fun getProfileBannerMobileRetinaURL(): String? =
        profileBannerImageUrl?.let { "$it/mobile_retina" }

    override fun getProfileBanner300x100URL(): String? =
        profileBannerImageUrl?.let { "$it/300x100" }

    override fun getProfileBanner600x200URL(): String? =
        profileBannerImageUrl?.let { "$it/600x200" }

    override fun getProfileBanner1500x500URL(): String? =
        profileBannerImageUrl?.let { "$it/1500x500" }

    override fun isProfileBackgroundTiled(): Boolean = profileBackgroundTiled
    override fun getLang(): String? = lang
    override fun getStatusesCount(): Int = statusesCount
    override fun isGeoEnabled(): Boolean = isGeoEnabled
    override fun isVerified(): Boolean = isVerified
    override fun isTranslator(): Boolean = translator
    override fun getListedCount(): Int = listedCount
    override fun isFollowRequestSent(): Boolean = isFollowRequestSent
    override fun getDescriptionURLEntities(): Array<URLEntity> = descriptionURLEntities
    override fun getURLEntity(): URLEntity? = urlEntities.firstOrNull()
    override fun getWithheldInCountries(): Array<String> = withheldInCountries

    override fun compareTo(other: User): Int = (id - other.id).toInt()

    /** **Not implemented** */
    override fun getRateLimitStatus(): RateLimitStatus? = null

    /** **Not implemented** */
    override fun getAccessLevel(): Int = -1

}
