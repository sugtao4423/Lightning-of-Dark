package sugtao4423.twitterweb4j.parser.model

import org.json.JSONException
import sugtao4423.twitter4j.HashtagEntity
import sugtao4423.twitter4j.MediaEntity
import sugtao4423.twitter4j.MediaResize
import sugtao4423.twitter4j.MediaSize
import sugtao4423.twitter4j.QuotedStatusPermalinkEntity
import sugtao4423.twitter4j.Size
import sugtao4423.twitter4j.SymbolEntity
import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.UserMentionEntity
import sugtao4423.twitter4j.VideoInfo
import sugtao4423.twitter4j.VideoVariant
import sugtao4423.twitterweb4j.Json

@Throws(JSONException::class)
fun parseSymbolEntity(json: Json): SymbolEntity {
    val start = json["indices"][0].int
    val end = json["indices"][1].int

    return SymbolEntity(start, end)
}

@Throws(JSONException::class)
fun parseHashtagEntity(json: Json): HashtagEntity {
    val text = json["text"].string
    val start = json["indices"][0].int
    val end = json["indices"][1].int

    return HashtagEntity(text, start, end)
}

@Throws(JSONException::class)
fun parseUrlEntity(json: Json): UrlEntity {
    val url = json["url"].string
    val expandedUrl = json["expanded_url"].stringOrNull
    val displayUrl = json["display_url"].string
    val start = json["indices"][0].int
    val end = json["indices"][1].int

    return UrlEntity(url, expandedUrl, displayUrl, start, end)
}

@Throws(JSONException::class)
fun parseUserMentionEntity(json: Json): UserMentionEntity {
    val id = json["id_str"].string.toLong()
    val name = json["name"].string
    val screenName = json["screen_name"].string
    val start = json["indices"][0].int
    val end = json["indices"][1].int

    return UserMentionEntity(id, name, screenName, start, end)
}

@Throws(JSONException::class)
fun parseQuotedStatusPermalinkEntity(json: Json): QuotedStatusPermalinkEntity {
    val url = json["url"].string
    val expandedUrl = json["expanded"].string
    val displayUrl = json["display"].string

    return QuotedStatusPermalinkEntity(url, expandedUrl, displayUrl)
}

@Throws(JSONException::class)
fun parseMediaEntity(json: Json): MediaEntity {
    val id = json["id_str"].string.toLong()
    val url = json["url"].string
    val mediaUrl = json["media_url_https"].string
    val expandedUrl = json["expanded_url"].string
    val displayUrl = json["display_url"].string

    val sizes: Map<MediaSize, Size> = json["sizes"].let { sizes ->
        val keys = listOf(
            Pair(MediaSize.LARGE, "large"),
            Pair(MediaSize.MEDIUM, "medium"),
            Pair(MediaSize.SMALL, "small"),
            Pair(MediaSize.THUMB, "thumb"),
        )
        keys.filter { (_, value) -> !sizes[value].isNull }.associate { (key, value) ->
            val width = sizes[value]["w"].int
            val height = sizes[value]["h"].int
            val resize =
                if (sizes[value]["resize"].string == "fit") MediaResize.FIT else MediaResize.CROP
            key to Size(width, height, resize)
        }
    }

    val type = json["type"].string
    val extAltText = json["ext_alt_text"].stringOrNull

    val videoInfo = json["video_info"].orNull()?.let {
        val aspectRatioWidth = it["aspect_ratio"][0].int
        val aspectRatioHeight = it["aspect_ratio"][1].int
        val durationMillis = it["duration_millis"].longOrNull
        val variants = it["variants"].let { v ->
            List(v.size) { i ->
                val bitrate = v[i]["bitrate"].intOrNull ?: 0
                val contentType = v[i]["content_type"].string
                val url = v[i]["url"].string
                VideoVariant(bitrate, contentType, url)
            }
        }
        VideoInfo(aspectRatioWidth, aspectRatioHeight, durationMillis, variants)
    }

    val start = json["indices"][0].int
    val end = json["indices"][1].int

    return MediaEntity(
        id,
        url,
        mediaUrl,
        expandedUrl,
        displayUrl,
        sizes,
        type,
        extAltText,
        videoInfo,
        start,
        end,
    )
}
