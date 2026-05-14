package sugtao4423.twitterweb4j.media

import androidx.core.net.toUri

internal object UploadUrl {

    private val baseUrl = "https://upload.twitter.com/i/media/upload.json".replace("twitter", "x")

    fun init(totalBytes: Long, mediaType: String, mediaCategory: String): String =
        baseUrl.toUri().buildUpon().apply {
            appendQueryParameter("command", "INIT")
            appendQueryParameter("total_bytes", totalBytes.toString())
            appendQueryParameter("media_type", mediaType)
            appendQueryParameter("media_category", mediaCategory)
        }.build().toString()

    fun appendMulti(
        mediaId: Long,
        segmentIndex: Int,
        maxSegmentSize: Long,
        mediaMd5: String,
    ): String =
        baseUrl.toUri().buildUpon().apply {
            appendQueryParameter("command", "APPENDMULTI")
            appendQueryParameter("media_id", mediaId.toString())
            appendQueryParameter("segment_indexes", segmentIndex.toString())
            appendQueryParameter("max_segment_size", maxSegmentSize.toString())
            appendQueryParameter("media_md5", mediaMd5)
        }.build().toString()

    fun finalize(mediaId: Long, originalMd5: String?): String =
        baseUrl.toUri().buildUpon().apply {
            appendQueryParameter("command", "FINALIZE")
            appendQueryParameter("media_id", mediaId.toString())
            originalMd5?.let { appendQueryParameter("original_md5", it) }
        }.build().toString()

    fun status(mediaId: Long): String =
        baseUrl.toUri().buildUpon().apply {
            appendQueryParameter("command", "STATUS")
            appendQueryParameter("media_id", mediaId.toString())
        }.build().toString()

}
