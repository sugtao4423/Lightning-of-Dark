package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.falseBoolean
import sugtao4423.twitterweb4j.nullString
import twitter4j.GeoLocation
import twitter4j.HashtagEntity
import twitter4j.JSONObject
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

data class StatusJSONImpl(@Transient val result: JSONObject) : Status, java.io.Serializable {

    @Transient
    private val json = result.nullString("__typename").let { typename ->
        when (typename) {
            "TweetWithVisibilityResults" -> result.getJSONObject("tweet")
            else -> result
        }
    }

    @Transient
    private val legacy = json.getJSONObject("legacy")

    @Transient
    private val entities = legacy.getJSONObject("entities")

    @Transient
    private val extendedEntities = legacy.optJSONObject("extended_entities")?.optJSONArray("media")
        ?: entities.optJSONArray("media")

    @Transient
    private val displayTextRange = legacy.optJSONArray("display_text_range")

    private val id = json.getString("rest_id").toLong()
    private val source = json.getString("source")
    private val createdAt = SimpleDateFormat(
        "EEE MMM dd HH:mm:ss Z yyyy", Locale.US
    ).parse(legacy.getString("created_at"))!!

    private val isTruncated = legacy.falseBoolean("truncated")
    private val inReplyToStatusId = legacy.optString("in_reply_to_status_id_str", "-1").toLong()
    private val inReplyToUserId = legacy.optString("in_reply_to_user_id_str", "-1").toLong()
    private val inReplyToScreenName = legacy.nullString("in_reply_to_screen_name")
    private val isFavorited = legacy.falseBoolean("favorited")
    private val isRetweeted = legacy.falseBoolean("retweeted")
    private val retweetCount = legacy.getInt("retweet_count")
    private val favoriteCount = legacy.getInt("favorite_count")
    private val isPossiblySensitive = legacy.falseBoolean("possibly_sensitive")

    private val user =
        json.getJSONObject("core").getJSONObject("user_results").getJSONObject("result").let {
            UserJSONImpl(it)
        }

    private val retweetedStatus =
        legacy.optJSONObject("retweeted_status_result")?.optJSONObject("result")?.let {
            StatusJSONImpl(it)
        }

    private val userMentionEntities: Array<UserMentionEntity> =
        entities.optJSONArray("user_mentions")?.let {
            (0 until it.length()).map { i -> UserMentionEntityJSONImpl(it.getJSONObject(i)) }
                .toTypedArray()
        } ?: emptyArray()
    private val urlEntities: Array<URLEntity> = entities.optJSONArray("urls")?.let {
        (0 until it.length()).map { i -> URLEntityJSONImpl(it.getJSONObject(i)) }.toTypedArray()
    } ?: emptyArray()
    private val hashtagEntities: Array<HashtagEntity> = entities.optJSONArray("hashtags")?.let {
        (0 until it.length()).map { i -> HashtagEntityJSONImpl(it.getJSONObject(i)) }.toTypedArray()
    } ?: emptyArray()
    private val symbolEntities: Array<SymbolEntity> = entities.optJSONArray("symbols")?.let {
        (0 until it.length()).map { i -> HashtagEntityJSONImpl(it.getJSONObject(i)) }.toTypedArray()
    } ?: emptyArray()

    private val mediaEntities: Array<MediaEntity> = extendedEntities?.let {
        (0 until it.length()).map { i -> MediaEntityJSONImpl(it.getJSONObject(i)) }.toTypedArray()
    } ?: emptyArray()

    private val quotedStatus =
        json.optJSONObject("quoted_status_result")?.optJSONObject("result")?.let {
            when (it.nullString("__typename")) {
                "TweetTombstone" -> null
                else -> StatusJSONImpl(it)
            }
        }
    private val quotedStatusId = legacy.optString("quoted_status_id_str", "-1").toLong()
    private val quotedStatusPermalink = legacy.optJSONObject("quoted_status_permalink")?.let {
        QuotedStatusPermalinkJSONImpl(it)
    }

    private val displayTextRangeStart = displayTextRange?.optInt(0, -1) ?: -1
    private val displayTextRangeEnd = displayTextRange?.optInt(1, -1) ?: -1

    private val text = legacy.getString("full_text")
    private val currentUserRetweetId = -1L
    private val lang = legacy.nullString("lang")

    private val withheldInCountries = legacy.optJSONArray("withheld_in_countries")?.let {
        (0 until it.length()).map { i -> it.getString(i) }.toTypedArray()
    } ?: emptyArray()

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
