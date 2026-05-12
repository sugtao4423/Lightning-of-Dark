package sugtao4423.twitterweb4j.parser.model

import org.json.JSONException
import sugtao4423.twitter4j.ProfileBanner
import sugtao4423.twitter4j.ProfileImage
import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.User
import sugtao4423.twitterweb4j.Json
import java.text.SimpleDateFormat
import java.util.Locale

@Throws(JSONException::class)
fun parseUserV1(json: Json): User {
    val id = json["id_str"].string.toLong()
    val name = json["name"].string
    val email = json["email"].stringOrNull
    val screenName = json["screen_name"].string
    val description = json["description"].stringOrNull
    val location = json["location"].stringOrNull
    val url = json["url"].stringOrNull
    val createdAt = json["created_at"].string.let {
        SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US).parse(it)
    } ?: throw JSONException("`created_at` not found")

    val descriptionUrlEntities = getUrlEntities(json, "description")
    val urlEntity = getUrlEntities(json, "url").firstOrNull()

    val profileImage = json["profile_image_url_https"].stringOrNull?.takeIf {
        it.isNotBlank()
    }?.let {
        ProfileImage(
            it,
            toResizedUrl(it, "_bigger"),
            toResizedUrl(it, "_mini"),
            toResizedUrl(it, ""),
            toResizedUrl(it, "_400x400"),
        )
    }
    val profileBanner = json["profile_banner_url"].stringOrNull?.takeIf {
        it.isNotBlank()
    }?.let {
        ProfileBanner(
            "$it/web",
            "$it/web_retina",
            "$it/ipad",
            "$it/ipad_retina",
            "$it/mobile",
            "$it/mobile_retina",
            "$it/300x100",
            "$it/600x200",
            "$it/1500x500",
        )
    }

    val statusesCount = json["statuses_count"].int
    val mediaCount = json["media_count"].int
    val favouritesCount = json["favourites_count"].int
    val friendsCount = json["friends_count"].int
    val followersCount = json["followers_count"].int
    val listedCount = json["listed_count"].int

    val profileBackgroundColor = json["profile_background_color"].stringOrNull
    val profileTextColor = json["profile_text_color"].stringOrNull
    val profileLinkColor = json["profile_link_color"].stringOrNull
    val profileSidebarFillColor = json["profile_sidebar_fill_color"].stringOrNull
    val profileSidebarBorderColor = json["profile_sidebar_border_color"].stringOrNull
    val isProfileUseBackgroundImage = json["profile_use_background_image"].boolOrFalse
    val isProfileBackgroundTiled = json["profile_background_tile"].boolOrFalse
    val profileBackgroundImageUrl = json["profile_background_image_url_https"].stringOrNull

    val isDefaultProfile = json["default_profile"].boolOrFalse
    val isDefaultProfileImage = json["default_profile_image"].boolOrFalse

    val utcOffset = json["utc_offset"].intOrNull
    val timeZone = json["time_zone"].stringOrNull

    val isProtected = json["protected"].boolOrFalse
    val isVerified = json["verified"].boolOrFalse
    val isContributorsEnabled = json["contributors_enabled"].boolOrFalse
    val isFollowRequestSent = json["follow_request_sent"].boolOrFalse
    val isGeoEnabled = json["geo_enabled"].boolOrFalse
    val isTranslator = json["is_translator"].boolOrFalse
    val lang = json["lang"].stringOrNull
    val withheldInCountries = json["withheld_in_countries"].let {
        List(it.size) { i -> it[i].string }
    }

    return User(
        id,
        name,
        email,
        screenName,
        description,
        location,
        url,
        createdAt,
        descriptionUrlEntities,
        urlEntity,
        profileImage,
        profileBanner,
        statusesCount,
        mediaCount,
        favouritesCount,
        friendsCount,
        followersCount,
        listedCount,
        profileBackgroundColor,
        profileTextColor,
        profileLinkColor,
        profileSidebarFillColor,
        profileSidebarBorderColor,
        isProfileUseBackgroundImage,
        isProfileBackgroundTiled,
        profileBackgroundImageUrl,
        isDefaultProfile,
        isDefaultProfileImage,
        utcOffset,
        timeZone,
        isProtected,
        isVerified,
        isContributorsEnabled,
        isFollowRequestSent,
        isGeoEnabled,
        isTranslator,
        lang,
        withheldInCountries,
    )
}

private fun getUrlEntities(json: Json, category: String): List<UrlEntity> {
    val urls = json["entities"][category]["urls"].orNull() ?: Json(emptyList<Any>())
    return List(urls.size) { parseUrlEntity(urls[it]) }
}

private fun toResizedUrl(originalURL: String, sizeSuffix: String): String {
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
