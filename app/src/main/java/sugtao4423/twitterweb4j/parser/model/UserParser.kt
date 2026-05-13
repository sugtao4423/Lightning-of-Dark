package sugtao4423.twitterweb4j.parser.model

import org.json.JSONException
import sugtao4423.twitter4j.ProfileBanner
import sugtao4423.twitter4j.ProfileImage
import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.User
import sugtao4423.twitterweb4j.Json

@Throws(JSONException::class)
fun parseUser(json: Json): User {
    val core = json["core"]
    val legacy = json["legacy"]

    val id = json["rest_id"].string.toLong()
    val name = legacy["name"].stringOrNull
        ?: core["name"].stringOrNull
        ?: "null"
    val email = legacy["email"].stringOrNull
    val screenName = legacy["screen_name"].stringOrNull
        ?: core["screen_name"].stringOrNull
        ?: "null"
    val description = legacy["description"].stringOrNull
    val location = legacy["location"].stringOrNull
        ?: json["location"]["location"].stringOrNull
    val url = legacy["url"].stringOrNull
    val createdAt = (legacy["created_at"].stringOrNull ?: core["created_at"].stringOrNull)?.let {
        parseTwitterDate(it)
    } ?: throw JSONException("`created_at` not found")

    val descriptionUrlEntities = getUrlEntities(json, "description")
    val urlEntity = getUrlEntities(json, "url").firstOrNull()

    val profileImage = (legacy["profile_image_url_https"].stringOrNull
        ?: json["avatar"]["image_url"].stringOrNull)?.takeIf { it.isNotBlank() }?.let {
        ProfileImage(
            it,
            toResizedUrl(it, "_bigger"),
            toResizedUrl(it, "_mini"),
            toResizedUrl(it, ""),
            toResizedUrl(it, "_400x400"),
        )
    }
    val profileBanner = legacy["profile_banner_url"].stringOrNull?.takeIf { it.isNotBlank() }?.let {
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

    val statusesCount = legacy["statuses_count"].int
    val mediaCount = legacy["media_count"].int
    val favouritesCount = legacy["favourites_count"].int
    val friendsCount = legacy["friends_count"].int
    val followersCount = legacy["followers_count"].int
    val listedCount = legacy["listed_count"].int

    val profileBackgroundColor = legacy["profile_background_color"].stringOrNull
    val profileTextColor = legacy["profile_text_color"].stringOrNull
    val profileLinkColor = legacy["profile_link_color"].stringOrNull
    val profileSidebarFillColor = legacy["profile_sidebar_fill_color"].stringOrNull
    val profileSidebarBorderColor = legacy["profile_sidebar_border_color"].stringOrNull
    val isProfileUseBackgroundImage = legacy["profile_use_background_image"].boolOrFalse
    val isProfileBackgroundTiled = legacy["profile_background_tile"].boolOrFalse
    val profileBackgroundImageUrl = legacy["profile_background_image_url_https"].stringOrNull

    val isDefaultProfile = legacy["default_profile"].boolOrFalse
    val isDefaultProfileImage = legacy["default_profile_image"].boolOrFalse

    val utcOffset = legacy["utc_offset"].intOrNull
    val timeZone = legacy["time_zone"].stringOrNull

    val isProtected = legacy["protected"].boolOrNull
        ?: json["privacy"]["protected"].boolOrFalse
    val isVerified = legacy["verified"].boolOrNull
        ?: json["verification"]["verified"].boolOrFalse
    val isContributorsEnabled = legacy["contributors_enabled"].boolOrFalse
    val isFollowRequestSent = legacy["follow_request_sent"].boolOrFalse
    val isGeoEnabled = legacy["geo_enabled"].boolOrFalse
    val isTranslator = legacy["is_translator"].boolOrFalse
    val lang = legacy["lang"].stringOrNull
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
    val urls = json["entities"][category].orNull()
        ?: json["legacy"]["entities"][category]["urls"].orNull()
        ?: Json(emptyList<Any>())
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
