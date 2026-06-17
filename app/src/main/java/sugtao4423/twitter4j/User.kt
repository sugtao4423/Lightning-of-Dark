package sugtao4423.twitter4j

import java.util.Date

data class ProfileImage(val url: String) : java.io.Serializable {
    val biggerUrl: String = toResizedUrl("_bigger")
    val miniUrl: String = toResizedUrl("_mini")
    val originalUrl: String = toResizedUrl("")
    val size400x400Url: String = toResizedUrl("_400x400")

    private fun toResizedUrl(sizeSuffix: String): String {
        val index = url.lastIndexOf("_")
        val suffixIndex = url.lastIndexOf(".")
        val slashIndex = url.lastIndexOf("/")
        val resized = url.substring(0, index) + sizeSuffix
        return if (suffixIndex > slashIndex) resized + url.substring(suffixIndex) else resized
    }
}

data class ProfileBanner(val baseUrl: String) : java.io.Serializable {
    val url: String = "$baseUrl/web"
    val retinaUrl: String = "$baseUrl/web_retina"
    val iPadUrl: String = "$baseUrl/ipad"
    val iPadRetinaUrl: String = "$baseUrl/ipad_retina"
    val mobileUrl: String = "$baseUrl/mobile"
    val mobileRetinaUrl: String = "$baseUrl/mobile_retina"
    val size300x100Url: String = "$baseUrl/300x100"
    val size600x200Url: String = "$baseUrl/600x200"
    val size1500x500Url: String = "$baseUrl/1500x500"
}

data class User(
    val id: Long,
    val name: String,
    val email: String?,
    val screenName: String,
    val description: String?,
    val location: String?,
    val url: String?,
    val createdAt: Date,

    val descriptionUrlEntities: List<UrlEntity>,
    val urlEntity: UrlEntity?,

    val profileImage: ProfileImage?,
    val profileBanner: ProfileBanner?,

    val statusesCount: Int,
    val mediaCount: Int,
    val favouritesCount: Int,
    val friendsCount: Int,
    val followersCount: Int,
    val listedCount: Int,

    val profileBackgroundColor: String?,
    val profileTextColor: String?,
    val profileLinkColor: String?,
    val profileSidebarFillColor: String?,
    val profileSidebarBorderColor: String?,
    val isProfileUseBackgroundImage: Boolean,
    val isProfileBackgroundTiled: Boolean,
    val profileBackgroundImageUrl: String?,

    val isDefaultProfile: Boolean,
    val isDefaultProfileImage: Boolean,

    val utcOffset: Int?,
    val timeZone: String?,

    val isProtected: Boolean,
    val isVerified: Boolean,
    val isContributorsEnabled: Boolean,
    val isFollowRequestSent: Boolean,
    val isGeoEnabled: Boolean,
    val isTranslator: Boolean,
    val lang: String?,
    val withheldInCountries: List<String>,
) : Comparable<User>, java.io.Serializable {

    override fun compareTo(other: User): Int = id.compareTo(other.id)

}
