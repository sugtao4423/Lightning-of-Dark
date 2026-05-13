package sugtao4423.twitterweb4j.parser.model

import org.json.JSONException
import sugtao4423.twitter4j.GeoLocation
import sugtao4423.twitter4j.Status
import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.parser.HtmlEntity

@Throws(JSONException::class)
fun parseStatus(result: Json): Status {
    val json = result["__typename"].stringOrNull.let { typename ->
        when (typename) {
            "TweetWithVisibilityResults" -> result["tweet"]
            else -> result
        }
    }
    val legacy = json["legacy"]
    val extendedEntities = legacy["extended_entities"]["media"].orNull()
        ?: legacy["entities"]["media"].orNull()
        ?: Json(emptyArray<Any>())

    val id = json["rest_id"].string.toLong()
    val displayTextRangeStart = legacy["display_text_range"][0].int
    val displayTextRangeEnd = legacy["display_text_range"][1].int
    val source = json["source"].string
    val createdAt = parseTwitterDate(legacy["created_at"].string)

    val isTruncated = legacy["truncated"].boolOrFalse
    val inReplyToStatusId = legacy["in_reply_to_status_id_str"].stringOrNull?.toLong()
    val inReplyToUserId = legacy["in_reply_to_user_id_str"].stringOrNull?.toLong()
    val inReplyToScreenName = legacy["in_reply_to_screen_name"].stringOrNull

    val geoLocation = legacy["geo"]["coordinates"].orNull()?.let {
        GeoLocation(it[0].double, it[1].double)
    }
    val place = legacy["place"].orNull()?.let { parsePlace(it) }

    val isPossiblySensitive = legacy["possibly_sensitive"].boolOrFalse
    val isRetweeted = legacy["retweeted"].boolOrFalse
    val isFavorited = legacy["favorited"].boolOrFalse
    val retweetCount = legacy["retweet_count"].int
    val favoriteCount = legacy["favorite_count"].int

    val retweetedStatus = legacy["retweeted_status_result"]["result"].orNull()?.let {
        parseStatus(it)
    }
    val isRetweet = retweetedStatus != null

    val quotedStatusId = legacy["quoted_status_id_str"].stringOrNull?.toLong()
    val quotedStatus = json["quoted_status_result"]["result"].orNull()?.let {
        when (it["__typename"].stringOrNull) {
            "TweetTombstone" -> null
            else -> parseStatus(it)
        }
    }
    val quotedStatusPermalink = legacy["quoted_status_permalink"].orNull()?.let {
        parseQuotedStatusPermalinkEntity(it)
    }

    val user = parseUser(json["core"]["user_results"]["result"])

    val unescaped = null.let {
        val text = legacy["full_text"].string
        val userMentionEntities = legacy["entities"]["user_mentions"].let {
            List(it.size) { i -> parseUserMentionEntity(it[i]) }
        }
        val urlEntities = legacy["entities"]["urls"].let {
            List(it.size) { i -> parseUrlEntity(it[i]) }
        }
        val hashtagEntities = legacy["entities"]["hashtags"].let {
            List(it.size) { i -> parseHashtagEntity(it[i]) }
        }
        val mediaEntities = extendedEntities.let {
            List(it.size) { i -> parseMediaEntity(it[i]) }
        }

        HtmlEntity.unescapeAndSlideEntityIndices(
            text, userMentionEntities, urlEntities, hashtagEntities, mediaEntities
        )
    }
    val symbolEntities = legacy["entities"]["symbols"].let {
        List(it.size) { i -> parseSymbolEntity(it[i]) }
    }

    val lang = legacy["lang"].stringOrNull
    val withheldInCountries = legacy["withheld_in_countries"].let {
        List(it.size) { i -> it[i].string }
    }

    return Status(
        id,
        unescaped.text,
        displayTextRangeStart,
        displayTextRangeEnd,
        source,
        createdAt,
        isTruncated,
        inReplyToStatusId,
        inReplyToUserId,
        inReplyToScreenName,
        geoLocation,
        place,
        isPossiblySensitive,
        isRetweeted,
        isFavorited,
        retweetCount,
        favoriteCount,
        retweetedStatus,
        isRetweet,
        quotedStatusId,
        quotedStatus,
        quotedStatusPermalink,
        user,
        unescaped.userMentions,
        unescaped.urls,
        unescaped.hashtags,
        unescaped.media,
        symbolEntities,
        lang,
        withheldInCountries,
    )
}
