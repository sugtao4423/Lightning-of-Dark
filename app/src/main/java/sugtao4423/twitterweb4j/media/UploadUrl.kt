package sugtao4423.twitterweb4j.media

import androidx.core.net.toUri

internal object UploadUrl {

    private val baseUrl = "https://upload.twitter.com/i/media/upload.json".replace("twitter", "x")
    private val baseUrl2 = "https://upload.twitter.com/i/media/upload2.json".replace("twitter", "x")

    private fun base(isVideo: Boolean) = if (isVideo) baseUrl2 else baseUrl

    fun init(
        isVideo: Boolean,
        totalBytes: Long,
        mediaType: String,
        mediaCategory: String,
        videoDurationMs: Long?,
    ): String =
        base(isVideo).toUri().buildUpon().apply {
            appendQueryParameter("command", "INIT")
            appendQueryParameter("total_bytes", totalBytes.toString())
            appendQueryParameter("media_type", mediaType)
            appendQueryParameter("media_category", mediaCategory)
            videoDurationMs?.let { appendQueryParameter("video_duration_ms", it.toString()) }
        }.build().toString()

    fun appendMulti(
        isVideo: Boolean,
        mediaId: Long,
        segmentIndex: Int,
        maxSegmentSize: Long,
        mediaMd5: String,
    ): String =
        base(isVideo).toUri().buildUpon().apply {
            appendQueryParameter("command", "APPENDMULTI")
            appendQueryParameter("media_id", mediaId.toString())
            appendQueryParameter("segment_indexes", segmentIndex.toString())
            appendQueryParameter("max_segment_size", maxSegmentSize.toString())
            appendQueryParameter("media_md5", mediaMd5)
        }.build().toString()

    fun finalize(
        isVideo: Boolean,
        mediaId: Long,
        originalMd5: String?,
    ): String =
        base(isVideo).toUri().buildUpon().apply {
            appendQueryParameter("command", "FINALIZE")
            appendQueryParameter("media_id", mediaId.toString())
            originalMd5?.let { appendQueryParameter("original_md5", it) }
            if (isVideo) appendQueryParameter("allow_async", "true")
        }.build().toString()

    fun status(isVideo: Boolean, mediaId: Long): String =
        base(isVideo).toUri().buildUpon().apply {
            appendQueryParameter("command", "STATUS")
            appendQueryParameter("media_id", mediaId.toString())
        }.build().toString()

}
