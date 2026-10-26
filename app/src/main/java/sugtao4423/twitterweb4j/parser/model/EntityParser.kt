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
import sugtao4423.twitterweb4j.mapList

@Throws(JSONException::class)
fun parseSymbolEntity(json: Json): SymbolEntity {
    val text = json["text"].string
    val start = json["indices"][0].int
    val end = json["indices"][1].int

    return SymbolEntity(text, start, end)
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
        keys.mapNotNull { (key, value) ->
            sizes[value].orNull()?.let { s ->
                val width = s["w"].int
                val height = s["h"].int
                val resize = if (s["resize"].string == "fit") MediaResize.FIT else MediaResize.CROP
                key to Size(width, height, resize)
            }
        }.toMap()
    }

    val extAltText = json["ext_alt_text"].stringOrNull

    val start = json["indices"][0].int
    val end = json["indices"][1].int

    val imageMediaEntity = MediaEntity.Image(
        id, url, mediaUrl, expandedUrl, displayUrl, sizes, extAltText, start, end
    )

    return when (val type = json["type"].string) {
        "photo" -> imageMediaEntity
        "animated_gif", "video" -> {
            val isGif = type == "animated_gif"

            val gifVideoInfo = json["video_info"].let {
                val aspectRatioWidth = it["aspect_ratio"][0].int
                val aspectRatioHeight = it["aspect_ratio"][1].int
                val variants = it["variants"].mapList { variant ->
                    val bitrate = variant["bitrate"].intOrNull ?: 0
                    val contentType = variant["content_type"].string
                    val url = variant["url"].string
                    VideoVariant(bitrate, contentType, url)
                }
                VideoInfo.Gif(aspectRatioWidth, aspectRatioHeight, variants)
            }

            if (isGif) {
                imageMediaEntity.toGif(gifVideoInfo)
            } else {
                val durationMillis = json["video_info"]["duration_millis"].long
                val videoInfo = gifVideoInfo.toVideo(durationMillis)
                imageMediaEntity.toVideo(videoInfo)
            }
        }

        else -> throw JSONException("Unsupported media type: $type")
    }
}

private fun VideoInfo.Gif.toVideo(durationMillis: Long) = VideoInfo.Video(
    aspectRatioWidth, aspectRatioHeight, durationMillis, variants
)

private fun MediaEntity.Image.toGif(videoInfo: VideoInfo.Gif) = MediaEntity.Gif(
    id, url, mediaUrl, expandedUrl, displayUrl, sizes, extAltText, videoInfo, start, end
)

private fun MediaEntity.Image.toVideo(videoInfo: VideoInfo.Video) = MediaEntity.Video(
    id, url, mediaUrl, expandedUrl, displayUrl, sizes, extAltText, videoInfo, start, end
)
