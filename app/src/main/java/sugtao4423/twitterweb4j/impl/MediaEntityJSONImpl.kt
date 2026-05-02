package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.model.EntityIndex
import twitter4j.MediaEntity

data class SizeJSONImpl(@Transient private val json: Json) : MediaEntity.Size,
    java.io.Serializable {

    private val width = json["w"].int
    private val height = json["h"].int
    private val resize = json["resize"].string

    override fun getWidth(): Int = width
    override fun getHeight(): Int = height
    override fun getResize(): Int =
        if (resize == "fit") MediaEntity.Size.FIT else MediaEntity.Size.CROP

}

data class VideoInfoJSONImpl(@Transient private val json: Json) : MediaEntity.Variant,
    java.io.Serializable {

    private val bitrate = json["bitrate"].intOrNull ?: 0
    private val contentType = json["content_type"].string
    private val url = json["url"].string

    override fun getBitrate(): Int = bitrate
    override fun getContentType(): String = contentType
    override fun getUrl(): String = url

}

data class MediaEntityJSONImpl(@Transient private val json: Json) : MediaEntity, EntityIndex(json),
    java.io.Serializable {

    private val id = json["id_str"].string.toLong()

    private val url = json["url"].string
    private val mediaURLHttps = json["media_url_https"].string
    private val expandedURL = json["expanded_url"].string
    private val displayURL = json["display_url"].string

    private val sizes: Map<Int, MediaEntity.Size> = json["sizes"].let { sizes ->
        val keys = listOf(
            Pair(MediaEntity.Size.LARGE, "large"),
            Pair(MediaEntity.Size.MEDIUM, "medium"),
            Pair(MediaEntity.Size.SMALL, "small"),
            Pair(MediaEntity.Size.THUMB, "thumb")
        )
        keys.filter { (_, value) -> !sizes[value].isNull }.associate { (key, value) ->
            key to SizeJSONImpl(sizes[value])
        }
    }

    private val type = json["type"].string

    private val aspectRatioWidth = json["video_info"]["aspect_ratio"][0].intOrNull ?: 0
    private val aspectRatioHeight = json["video_info"]["aspect_ratio"][1].intOrNull ?: 0
    private val videoDurationMillis = json["video_info"]["duration_millis"].longOrNull ?: 0
    private val videoVariants: Array<MediaEntity.Variant> =
        json["video_info"]["variants"].let { v ->
            Array(v.size) { i -> VideoInfoJSONImpl(v[i]) }
        }

    private val extAltText = json["ext_alt_text"].stringOrNull

    override fun getId(): Long = id
    override fun getMediaURL(): String = mediaURLHttps
    override fun getMediaURLHttps(): String = mediaURLHttps
    override fun getText(): String = url
    override fun getURL(): String = url
    override fun getDisplayURL(): String = displayURL
    override fun getExpandedURL(): String = expandedURL
    override fun getSizes(): Map<Int, MediaEntity.Size> = sizes
    override fun getType(): String = type

    override fun getVideoAspectRatioWidth(): Int = aspectRatioWidth
    override fun getVideoAspectRatioHeight(): Int = aspectRatioHeight
    override fun getVideoDurationMillis(): Long = videoDurationMillis
    override fun getVideoVariants(): Array<MediaEntity.Variant> = videoVariants

    override fun getExtAltText(): String? = extAltText

}
