package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.Json
import twitter4j.RateLimitStatus
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.URLEntity
import twitter4j.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserJSONImpl(@Transient private val json: Json) : User, java.io.Serializable {
    @Transient
    private val core = json["core"]

    @Transient
    private val legacy = json["legacy"]

    private val id = json["rest_id"].string.toLong()

    private val name = legacy["name"].orNull()?.string
        ?: core["name"].orNull()?.string
        ?: "null"
    private val email = legacy["email"].stringOrNull
    private val screenName = legacy["screen_name"].orNull()?.string
        ?: core["screen_name"].orNull()?.string
        ?: "null"
    private val location = legacy["location"].orNull()?.string
        ?: json["location"]["location"].orNull()?.string
        ?: "null"

    private val descriptionURLEntities = getURLEntities("description")
    private val urlEntities = getURLEntities("url")
    private val description = legacy["description"].string

    private val isContributorsEnabled = legacy["contributors_enabled"].boolOrFalse
    private val profileImageUrlHttps = legacy["profile_image_url_https"].orNull()?.string
        ?: json["avatar"]["image_url"].orNull()?.string
        ?: throw TwitterException("Profile image URL not found")
    private val isDefaultProfileImage = legacy["default_profile_image"].boolOrFalse
    private val url = legacy["url"].stringOrNull
    private val isProtected = legacy["protected"].orNull()?.bool
        ?: json["privacy"]["protected"].orNull()?.bool
        ?: false
    private val isGeoEnabled = legacy["geo_enabled"].boolOrFalse
    private val isVerified = legacy["verified"].orNull()?.bool
        ?: json["verification"]["verified"].orNull()?.bool
        ?: false
    private val translator = legacy["is_translator"].boolOrFalse
    private val followersCount = legacy["followers_count"].int

    private val profileBackgroundColor = legacy["profile_background_color"].stringOrNull
    private val profileTextColor = legacy["profile_text_color"].stringOrNull
    private val profileLinkColor = legacy["profile_link_color"].stringOrNull
    private val profileSidebarFillColor = legacy["profile_sidebar_fill_color"].stringOrNull
    private val profileSidebarBorderColor = legacy["profile_sidebar_border_color"].stringOrNull
    private val profileUseBackgroundImage = legacy["profile_use_background_image"].boolOrFalse
    private val isDefaultProfile = legacy["default_profile"].boolOrFalse
    private val showAllInlineMedia = legacy["show_all_inline_media"].boolOrFalse
    private val friendsCount = legacy["friends_count"].int
    private val createdAt = (
            legacy["created_at"].orNull()?.string
                ?: core["created_at"].orNull()?.string
            )?.let { SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US).parse(it) }
        ?: throw TwitterException("Created at date not found")
    private val favouritesCount = legacy["favourites_count"].int
    private val utcOffset = legacy["utc_offset"].intOrNull ?: 0
    private val timeZone = legacy["time_zone"].stringOrNull
    private val profileBackgroundImageUrlHttps =
        legacy["profile_background_image_url_https"].stringOrNull
    private val profileBannerImageUrl = legacy["profile_banner_url"].stringOrNull
    private val profileBackgroundTiled = legacy["profile_background_tile"].boolOrFalse
    private val lang = legacy["lang"].stringOrNull
    private val statusesCount = legacy["statuses_count"].int
    private val listedCount = legacy["listed_count"].int
    private val isFollowRequestSent = legacy["follow_request_sent"].boolOrFalse

    private val withheldInCountries = json["withheld_in_countries"].let {
        Array(it.size) { i -> it[i].string }
    }

    private fun getURLEntities(category: String): Array<URLEntity> {
        val urls = json["entities"][category].orNull()
            ?: legacy["entities"][category]["urls"].orNull()
            ?: Json(emptyArray<Any>())
        return Array(urls.size) { URLEntityJSONImpl(urls[it]) }
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

    override fun compareTo(other: User): Int = id.compareTo(other.id)

    /** **Not implemented** */
    override fun getRateLimitStatus(): RateLimitStatus? = null

    /** **Not implemented** */
    override fun getAccessLevel(): Int = -1

}
