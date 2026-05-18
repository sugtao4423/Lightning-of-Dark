package sugtao4423.twitterweb4j.media

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

internal object UploadUrl {

    private val baseUrl = "https://upload.twitter.com/i/media/upload.json".replace("twitter", "x")
    private val baseUrl2 = "https://upload.twitter.com/i/media/upload2.json".replace("twitter", "x")

    private fun baseHttpUrl(isVideo: Boolean) = (if (isVideo) baseUrl2 else baseUrl).toHttpUrl()

    fun init(
        isVideo: Boolean,
        totalBytes: Long,
        mediaType: String,
        mediaCategory: String,
        videoDurationMs: Long?,
    ): HttpUrl = baseHttpUrl(isVideo).newBuilder().apply {
        addQueryParameter("command", "INIT")
        addQueryParameter("total_bytes", totalBytes.toString())
        addQueryParameter("media_type", mediaType)
        addQueryParameter("media_category", mediaCategory)
        videoDurationMs?.let { addQueryParameter("video_duration_ms", it.toString()) }
    }.build()

    fun appendMulti(
        isVideo: Boolean,
        mediaId: Long,
        segmentIndex: Int,
        maxSegmentSize: Long,
        mediaMd5: String,
    ): HttpUrl = baseHttpUrl(isVideo).newBuilder().apply {
        addQueryParameter("command", "APPENDMULTI")
        addQueryParameter("media_id", mediaId.toString())
        addQueryParameter("segment_indexes", segmentIndex.toString())
        addQueryParameter("max_segment_size", maxSegmentSize.toString())
        addQueryParameter("media_md5", mediaMd5)
    }.build()

    fun finalize(
        isVideo: Boolean,
        mediaId: Long,
        originalMd5: String?,
    ): HttpUrl = baseHttpUrl(isVideo).newBuilder().apply {
        addQueryParameter("command", "FINALIZE")
        addQueryParameter("media_id", mediaId.toString())
        originalMd5?.let { addQueryParameter("original_md5", it) }
        if (isVideo) addQueryParameter("allow_async", "true")
    }.build()

    fun status(isVideo: Boolean, mediaId: Long): HttpUrl = baseHttpUrl(isVideo).newBuilder().apply {
        addQueryParameter("command", "STATUS")
        addQueryParameter("media_id", mediaId.toString())
    }.build()

}
