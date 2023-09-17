package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable

data class MediaEntity(private val json: JSONObject) : Serializable {

    enum class MediaSize(val value: String) {
        LARGE("large"), MEDIUM("medium"), SMALL("small"), THUMB("thumb")
    }

    val id: Long = json.getString("id_str").toLong()

    val url: String = json.getString("url")
    val expandedUrl: String = json.getString("expanded_url")
    val displayUrl: String = json.getString("display_url")
    val mediaUrlHttps: String = json.getString("media_url_https")

    private val indices = json.getJSONArray("indices")
    val start: Int = indices.getInt(0)
    val end: Int = indices.getInt(1)

    val type: String = json.getString("type")

    val sizes: Map<MediaSize, Size> = json.getJSONObject("sizes").let {
        val result = mutableMapOf<MediaSize, Size>()
        MediaSize.values().filter { size -> it.has(size.value) }.forEach { size ->
            result[size] = Size(it.getJSONObject(size.value))
        }
        result.toMap()
    }
    val originalInfo = OriginalInfo(json.getJSONObject("original_info"))

    val videoInfo: VideoInfo? = json.optJSONObject("video_info")?.let { VideoInfo(it) }


    data class Size(private val json: JSONObject) : Serializable {

        enum class Resize(val value: String) {
            FIT("fit"), CROP("crop")
        }

        val w: Int = json.getInt("w")
        val h: Int = json.getInt("h")
        val resize: Resize = if (json.getString("resize") === Resize.FIT.value) {
            Resize.FIT
        } else {
            Resize.CROP
        }
    }

    data class OriginalInfo(private val json: JSONObject) : Serializable {

        val width: Int = json.getInt("width")
        val height: Int = json.getInt("height")
        val focusRects: List<FocusRect> = json.getJSONArray("focus_rects").let {
            (0 until it.length()).map { i -> FocusRect(it.getJSONObject(i)) }
        }

        data class FocusRect(private val json: JSONObject) : Serializable {
            val x: Int = json.getInt("x")
            val y: Int = json.getInt("y")
            val w: Int = json.getInt("w")
            val h: Int = json.getInt("h")
        }

    }

    data class VideoInfo(private val json: JSONObject) : Serializable {

        private val aspectRatio = json.getJSONArray("aspect_ratio")
        val aspectRatioWidth: Int = aspectRatio.getInt(0)
        val aspectRatioHeight: Int = aspectRatio.getInt(1)

        val durationMillis: Int = json.getInt("duration_millis")

        val variants: List<Variant> = json.getJSONArray("variants").let {
            (0 until it.length()).map { i -> Variant(it.getJSONObject(i)) }
        }

        data class Variant(private val json: JSONObject) : Serializable {
            val bitrate: Int = json.optInt("bitrate", 0)
            val contentType: String = json.getString("content_type")
            val url: String = json.getString("url")
        }

    }

}
