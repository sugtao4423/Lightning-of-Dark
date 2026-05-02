package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.Json
import twitter4j.GeoLocation
import twitter4j.HashtagEntity
import twitter4j.MediaEntity
import twitter4j.Place
import twitter4j.RateLimitStatus
import twitter4j.Scopes
import twitter4j.Status
import twitter4j.SymbolEntity
import twitter4j.URLEntity
import twitter4j.User
import twitter4j.UserMentionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StatusJSONImpl(@Transient val result: Json) : Status, java.io.Serializable {

    @Transient
    private val json = result["__typename"].stringOrNull.let { typename ->
        when (typename) {
            "TweetWithVisibilityResults" -> result["tweet"]
            else -> result
        }
    }

    @Transient
    private val legacy = json["legacy"]

    @Transient
    private val extendedEntities = legacy["extended_entities"]["media"].orNull()
        ?: legacy["entities"]["media"].orNull()
        ?: Json(emptyArray<Any>())

    private val id = json["rest_id"].string.toLong()
    private val source = json["source"].string
    private val createdAt = SimpleDateFormat(
        "EEE MMM dd HH:mm:ss Z yyyy", Locale.US
    ).parse(legacy["created_at"].string)

    private val isTruncated = legacy["truncated"].boolOrFalse
    private val inReplyToStatusId =
        legacy["in_reply_to_status_id_str"].stringOrNull?.toLong() ?: -1L
    private val inReplyToUserId = legacy["in_reply_to_user_id_str"].stringOrNull?.toLong() ?: -1L
    private val inReplyToScreenName = legacy["in_reply_to_screen_name"].stringOrNull
    private val isFavorited = legacy["favorited"].boolOrFalse
    private val isRetweeted = legacy["retweeted"].boolOrFalse
    private val retweetCount = legacy["retweet_count"].int
    private val favoriteCount = legacy["favorite_count"].int
    private val isPossiblySensitive = legacy["possibly_sensitive"].boolOrFalse

    private val user = UserJSONImpl(json["core"]["user_results"]["result"])

    private val retweetedStatus = legacy["retweeted_status_result"]["result"].orNull()?.let {
        StatusJSONImpl(it)
    }

    private val userMentionEntities: Array<UserMentionEntity> =
        legacy["entities"]["user_mentions"].let {
            Array(it.size) { i -> UserMentionEntityJSONImpl(it[i]) }
        }
    private val urlEntities: Array<URLEntity> = legacy["entities"]["urls"].let {
        Array(it.size) { i -> URLEntityJSONImpl(it[i]) }
    }
    private val hashtagEntities: Array<HashtagEntity> = legacy["entities"]["hashtags"].let {
        Array(it.size) { i -> HashtagEntityJSONImpl(it[i]) }
    }
    private val symbolEntities: Array<SymbolEntity> = legacy["entities"]["symbols"].let {
        Array(it.size) { i -> HashtagEntityJSONImpl(it[i]) }
    }

    private val mediaEntities: Array<MediaEntity> = extendedEntities.let {
        Array(it.size) { i -> MediaEntityJSONImpl(it[i]) }
    }

    private val quotedStatus = json["quoted_status_result"]["result"].orNull()?.let {
        when (it["__typename"].stringOrNull) {
            "TweetTombstone" -> null
            else -> StatusJSONImpl(it)
        }
    }
    private val quotedStatusId = legacy["quoted_status_id_str"].stringOrNull?.toLong() ?: -1L
    private val quotedStatusPermalink = legacy["quoted_status_permalink"].orNull()?.let {
        QuotedStatusPermalinkJSONImpl(it)
    }

    private val displayTextRangeStart = legacy["display_text_range"][0].intOrNull ?: -1
    private val displayTextRangeEnd = legacy["display_text_range"][1].intOrNull ?: -1

    private val text = legacy["full_text"].string
    private val currentUserRetweetId = -1L
    private val lang = legacy["lang"].stringOrNull

    private val withheldInCountries = legacy["withheld_in_countries"].let {
        Array(it.size) { i -> it[i].string }
    }

    override fun getCreatedAt(): Date = createdAt
    override fun getId(): Long = id
    override fun getText(): String = text
    override fun getDisplayTextRangeStart(): Int = displayTextRangeStart
    override fun getDisplayTextRangeEnd(): Int = displayTextRangeEnd
    override fun getSource(): String = source
    override fun isTruncated(): Boolean = isTruncated
    override fun getInReplyToStatusId(): Long = inReplyToStatusId
    override fun getInReplyToUserId(): Long = inReplyToUserId
    override fun getInReplyToScreenName(): String? = inReplyToScreenName

    /** **Not implemented** */
    override fun getGeoLocation(): GeoLocation? = null

    /** **Not implemented** */
    override fun getPlace(): Place? = null

    override fun isFavorited(): Boolean = isFavorited
    override fun isRetweeted(): Boolean = isRetweeted
    override fun getFavoriteCount(): Int = favoriteCount
    override fun getUser(): User = user
    override fun isRetweet(): Boolean = retweetedStatus != null
    override fun getRetweetedStatus(): Status? = retweetedStatus

    /** **Not implemented** */
    override fun getContributors(): LongArray = longArrayOf()

    override fun getRetweetCount(): Int = retweetCount

    /** **Not implemented** */
    override fun isRetweetedByMe(): Boolean = currentUserRetweetId != -1L

    /** **Not implemented** */
    override fun getCurrentUserRetweetId(): Long = currentUserRetweetId

    override fun isPossiblySensitive(): Boolean = isPossiblySensitive

    override fun getUserMentionEntities(): Array<UserMentionEntity> = userMentionEntities
    override fun getURLEntities(): Array<URLEntity> = urlEntities
    override fun getHashtagEntities(): Array<HashtagEntity> = hashtagEntities
    override fun getSymbolEntities(): Array<SymbolEntity> = symbolEntities
    override fun getMediaEntities(): Array<MediaEntity> = mediaEntities

    override fun getLang(): String? = lang
    override fun getScopes(): Scopes? = null
    override fun getWithheldInCountries(): Array<String> = withheldInCountries
    override fun getQuotedStatusId(): Long = quotedStatusId
    override fun getQuotedStatus(): Status? = quotedStatus
    override fun getQuotedStatusPermalink(): URLEntity? = quotedStatusPermalink

    override fun compareTo(other: Status): Int = id.compareTo(other.id)

    /** **Not implemented** */
    override fun getRateLimitStatus(): RateLimitStatus? = null

    /** **Not implemented** */
    override fun getAccessLevel(): Int = -1

}
