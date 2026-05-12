package sugtao4423.twitter4j

import twitter4j.HashtagEntity
import twitter4j.MediaEntity
import twitter4j.SymbolEntity
import twitter4j.URLEntity
import twitter4j.User
import twitter4j.UserMentionEntity
import java.util.Date

data class Status(
    val id: Long,
    val text: String,
    val displayTextRangeStart: Int,
    val displayTextRangeEnd: Int,
    val source: String,
    val createdAt: Date,

    val isTruncated: Boolean,
    val inReplyToStatusId: Long?,
    val inReplyToUserId: Long?,
    val inReplyToScreenName: String?,

    val geoLocation: GeoLocation?,
    val place: Place?,

    val isPossiblySensitive: Boolean,
    val isRetweeted: Boolean,
    val isFavorited: Boolean,
    val retweetCount: Int,
    val favoriteCount: Int,

    val retweetedStatus: Status?,
    val isRetweet: Boolean,

    val quotedStatusId: Long?,
    val quotedStatus: Status?,
    val quotedStatusPermalink: URLEntity?,

    val user: User,

    val userMentionEntities: List<UserMentionEntity>,
    val urlEntities: List<URLEntity>,
    val hashtagEntities: List<HashtagEntity>,
    val mediaEntities: List<MediaEntity>,
    val symbolEntities: List<SymbolEntity>,

    val lang: String?,
    val withheldInCountries: List<String>?,
) : Comparable<Status>, java.io.Serializable {

    override fun compareTo(other: Status): Int = id.compareTo(other.id)

}
