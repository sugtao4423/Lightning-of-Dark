package sugtao4423.twitter4j

import twitter4j.URLEntity
import java.util.Date

data class ProfileImage(
    val url: String,
    val biggerUrl: String,
    val miniUrl: String,
    val originalUrl: String,
    val size400x400Url: String,
) : java.io.Serializable

data class ProfileBanner(
    val url: String,
    val retinaUrl: String,
    val iPadUrl: String,
    val iPadRetinaUrl: String,
    val mobileUrl: String,
    val mobileRetinaUrl: String,
    val size300x100Url: String,
    val size600x200Url: String,
    val size1500x500Url: String,
) : java.io.Serializable

data class User(
    val id: Long,
    val name: String,
    val email: String?,
    val screenName: String,
    val description: String?,
    val location: String?,
    val url: String?,
    val createdAt: Date,

    val descriptionUrlEntities: List<URLEntity>,
    val urlEntity: URLEntity?,

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
