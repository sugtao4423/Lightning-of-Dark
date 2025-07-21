package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.model.EntityIndex
import sugtao4423.twitterweb4j.nullString
import twitter4j.JSONObject
import twitter4j.MediaEntity

data class SizeJSONImpl(@Transient private val json: JSONObject) : MediaEntity.Size,
    java.io.Serializable {

    private val width = json.getInt("w")
    private val height = json.getInt("h")
    private val resize = json.getString("resize")

    override fun getWidth(): Int = width
    override fun getHeight(): Int = height
    override fun getResize(): Int =
        if (resize == "fit") MediaEntity.Size.FIT else MediaEntity.Size.CROP

}

data class VideoInfoJSONImpl(@Transient private val json: JSONObject) : MediaEntity.Variant,
    java.io.Serializable {

    private val bitrate = json.optInt("bitrate", 0)
    private val contentType = json.getString("content_type")
    private val url = json.getString("url")

    override fun getBitrate(): Int = bitrate
    override fun getContentType(): String = contentType
    override fun getUrl(): String = url

}

data class MediaEntityJSONImpl(@Transient private val json: JSONObject) : MediaEntity,
    EntityIndex(json), java.io.Serializable {

    @Transient
    private val videoInfo = json.optJSONObject("video_info")

    @Transient
    private val videoAspectRatio = videoInfo?.optJSONArray("aspect_ratio")

    private val id = json.getString("id_str").toLong()

    private val url = json.getString("url")
    private val mediaURLHttps = json.getString("media_url_https")
    private val expandedURL = json.getString("expanded_url")
    private val displayURL = json.getString("display_url")

    private val sizes: MutableMap<Int, MediaEntity.Size> =
        json.getJSONObject("sizes").let { sizes ->
            val keys = listOf(
                Pair(MediaEntity.Size.LARGE, "large"),
                Pair(MediaEntity.Size.MEDIUM, "medium"),
                Pair(MediaEntity.Size.SMALL, "small"),
                Pair(MediaEntity.Size.THUMB, "thumb")
            )
            val result = mutableMapOf<Int, MediaEntity.Size>()
            keys.filter { (_, value) -> sizes.has(value) }.forEach { (key, value) ->
                result[key] = SizeJSONImpl(sizes.getJSONObject(value))
            }
            result
        }

    private val type = json.getString("type")

    private val aspectRatioWidth = videoAspectRatio?.optInt(0, 0) ?: 0
    private val aspectRatioHeight = videoAspectRatio?.optInt(1, 0) ?: 0
    private val videoDurationMillis = videoInfo?.optLong("duration_millis", 0) ?: 0
    private val videoVariants: Array<MediaEntity.Variant> =
        videoInfo?.optJSONArray("variants")?.let {
            (0 until it.length()).map { i -> VideoInfoJSONImpl(it.getJSONObject(i)) }.toTypedArray()
        } ?: emptyArray()

    private val extAltText = json.nullString("ext_alt_text")

    override fun getId(): Long = id
    override fun getMediaURL(): String = mediaURLHttps
    override fun getMediaURLHttps(): String = mediaURLHttps
    override fun getText(): String = url
    override fun getURL(): String = url
    override fun getDisplayURL(): String = displayURL
    override fun getExpandedURL(): String = expandedURL
    override fun getSizes(): MutableMap<Int, MediaEntity.Size> = sizes
    override fun getType(): String = type

    override fun getVideoAspectRatioWidth(): Int = aspectRatioWidth
    override fun getVideoAspectRatioHeight(): Int = aspectRatioHeight
    override fun getVideoDurationMillis(): Long = videoDurationMillis
    override fun getVideoVariants(): Array<MediaEntity.Variant> = videoVariants

    override fun getExtAltText(): String? = extAltText

}
