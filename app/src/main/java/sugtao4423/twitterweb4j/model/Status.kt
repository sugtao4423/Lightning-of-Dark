package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Status(private val json: JSONObject) : Comparable<Status>, Serializable {

    val id: Long
    val text: String
    val user: User

    val displayTextRangeStart: Int
    val displayTextRangeEnd: Int
    val source: String

    val inReplyToScreenName: String?
    val inReplyToStatusId: Long?
    val inReplyToUserId: Long?

    val isFavotired: Boolean
    val isRetweeted: Boolean
    val isQuoteStatus: Boolean

    val favoriteCount: Int
    val retweetCount: Int
    val replyCount: Int
    val quoteCount: Int

    val retweetedStatus: Status?

    val userMentionEntities: List<UserMentionEntity>
    val urlEntities: List<URLEntity>
    val hashtagEntities: List<HashtagEntity>
    val symbolEntities: List<SymbolEntity>
    val mediaEntities: List<MediaEntity>

    val quotedStatusId: Long?
    val quotedStatusPermalink: QuotedStatusPermalink?
    val quotedStatus: Status?

    val lang: String
    val createdAt: Date

    init {
        id = json.getString("rest_id").toLong()
        source = json.getString("source")
        user =
            User(json.getJSONObject("core").getJSONObject("user_results").getJSONObject("result"))

        val legacy = json.getJSONObject("legacy")
        text = legacy.getString("full_text")

        val displayTextRange = legacy.getJSONArray("display_text_range")
        displayTextRangeStart = displayTextRange.getInt(0)
        displayTextRangeEnd = displayTextRange.getInt(1)

        inReplyToScreenName = legacy.optString("in_reply_to_screen_name", "").ifEmpty { null }
        inReplyToStatusId = legacy.optString("in_reply_to_status_id_str", "").ifEmpty {
            null
        }?.toLong()
        inReplyToUserId = legacy.optString("in_reply_to_user_id_str", "").ifEmpty {
            null
        }?.toLong()

        isFavotired = legacy.getBoolean("favorited")
        isRetweeted = legacy.getBoolean("retweeted")
        isQuoteStatus = legacy.getBoolean("is_quote_status")

        favoriteCount = legacy.getInt("favorite_count")
        retweetCount = legacy.getInt("retweet_count")
        replyCount = legacy.getInt("reply_count")
        quoteCount = legacy.getInt("quote_count")

        retweetedStatus = legacy.optJSONObject("retweeted_status_result")?.let {
            Status(it.getJSONObject("result"))
        }

        val entities = legacy.getJSONObject("entities")
        userMentionEntities = entities.getJSONArray("user_mentions").let {
            (0 until it.length()).map { i -> UserMentionEntity(it.getJSONObject(i)) }
        }
        urlEntities = entities.getJSONArray("urls").let {
            (0 until it.length()).map { i -> URLEntity(it.getJSONObject(i)) }
        }
        hashtagEntities = entities.getJSONArray("hashtags").let {
            (0 until it.length()).map { i -> HashtagEntity(it.getJSONObject(i)) }
        }
        symbolEntities = entities.getJSONArray("symbols").let {
            (0 until it.length()).map { i -> SymbolEntity(it.getJSONObject(i)) }
        }
        mediaEntities = entities.optJSONArray("media")?.let {
            (0 until it.length()).map { i -> MediaEntity(it.getJSONObject(i)) }
        } ?: emptyList()

        quotedStatusId = legacy.optString("quoted_status_id_str", "").ifEmpty {
            null
        }?.toLong()
        quotedStatusPermalink = legacy.optJSONObject("quoted_status_permalink")?.let {
            QuotedStatusPermalink(it)
        }
        quotedStatus = json.optJSONObject("quoted_status_result")?.getJSONObject("result")?.let {
            Status(it)
        }

        lang = legacy.getString("lang")
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US)
        createdAt = dateFormat.parse(legacy.getString("created_at"))!!
    }

    override fun compareTo(other: Status): Int {
        return (id - other.id).toInt()
    }

}
