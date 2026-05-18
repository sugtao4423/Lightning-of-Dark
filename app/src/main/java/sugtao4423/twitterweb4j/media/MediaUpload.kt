package sugtao4423.twitterweb4j.media

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import sugtao4423.twitter4j.TwitterException
import sugtao4423.twitterweb4j.Connection
import sugtao4423.twitterweb4j.media.UploadJsonParser.ProcessingInfo
import java.io.IOException
import java.security.MessageDigest

class MediaUpload internal constructor(
    private val client: OkHttpClient, cookie: String, csrfToken: String
) {

    companion object {
        private const val MEDIA_CATEGORY_IMAGE = "tweet_image"
        private const val MEDIA_CATEGORY_GIF = "tweet_gif"
        private const val MEDIA_CATEGORY_VIDEO = "amplify_video"

        private const val MAX_SEGMENT_SIZE = 8 * 1024 * 1024 // 8 MiB

        private val CONTENT_TYPE_OCTET_STREAM = "application/octet-stream".toMediaType()
    }

    private val headers = Connection.authorizedHeaders.newBuilder().apply {
        add("Cookie", cookie)
        add("X-Csrf-Token", csrfToken)
    }.build()

    @Throws(TwitterException::class)
    fun upload(data: ByteArray, mediaType: String, videoDurationMs: Long? = null): Long {
        val isVideo = mediaType.startsWith("video/")
        if (isVideo && videoDurationMs == null) {
            throw TwitterException("videoDurationMs is required for video upload.")
        }

        val mediaCategory = when {
            isVideo -> MEDIA_CATEGORY_VIDEO
            mediaType == "image/gif" -> MEDIA_CATEGORY_GIF
            else -> MEDIA_CATEGORY_IMAGE
        }

        val mediaId = init(isVideo, data.size.toLong(), mediaType, mediaCategory, videoDurationMs)
        appendMulti(isVideo, mediaId, data)

        val originalMd5 = if (mediaCategory == MEDIA_CATEGORY_IMAGE) md5(data) else null
        val finalizeResponse = finalize(isVideo, mediaId, originalMd5)
        UploadJsonParser.parseProcessingInfo(finalizeResponse)?.let {
            waitForProcessing(isVideo, mediaId, it)
        }
        return mediaId
    }

    @Throws(TwitterException::class)
    private fun init(
        isVideo: Boolean,
        totalBytes: Long,
        mediaType: String,
        mediaCategory: String,
        videoDurationMs: Long?,
    ): Long {
        val url = UploadUrl.init(isVideo, totalBytes, mediaType, mediaCategory, videoDurationMs)
        val response = post(url)
        return UploadJsonParser.parseUploadMedia(response)
    }

    @Throws(TwitterException::class)
    private fun appendMulti(isVideo: Boolean, mediaId: Long, data: ByteArray) {
        var segmentIndex = 0
        var offset = 0
        while (offset < data.size) {
            val end = (offset + MAX_SEGMENT_SIZE).coerceAtMost(data.size)
            val chunk = data.copyOfRange(offset, end)

            val url = UploadUrl.appendMulti(
                isVideo, mediaId, segmentIndex, chunk.size.toLong(), md5(chunk)
            )
            val multipart = MultipartBody.Builder().apply {
                setType(MultipartBody.FORM)
                addFormDataPart("media", "blob", chunk.toRequestBody(CONTENT_TYPE_OCTET_STREAM))
            }.build()
            post(url, multipart)

            offset = end
            segmentIndex++
        }
    }

    @Throws(TwitterException::class)
    private fun finalize(isVideo: Boolean, mediaId: Long, originalMd5: String?): String {
        val url = UploadUrl.finalize(isVideo, mediaId, originalMd5)
        return post(url)
    }

    @Throws(TwitterException::class)
    private fun waitForProcessing(isVideo: Boolean, mediaId: Long, initial: ProcessingInfo) {
        var info = initial
        while (info.state == ProcessingInfo.STATE_PENDING || info.state == ProcessingInfo.STATE_IN_PROGRESS) {
            Thread.sleep((info.checkAfterSecs ?: 1) * 1000L)
            val url = UploadUrl.status(isVideo, mediaId)
            val response = get(url)
            info = UploadJsonParser.parseProcessingInfo(response)
                ?: throw TwitterException("Missing processing_info in STATUS response.")
        }
        if (info.state != ProcessingInfo.STATE_SUCCEEDED) {
            throw TwitterException("Media upload failed: state=${info.state}")
        }
    }

    private fun md5(data: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(data).joinToString("") { "%02x".format(it) }

    @Throws(TwitterException::class)
    private fun get(url: String): String = execute("GET", url)

    @Throws(TwitterException::class)
    private fun post(url: String, body: RequestBody = RequestBody.EMPTY): String =
        execute("POST", url, body)

    @Throws(TwitterException::class)
    private fun execute(method: String, url: String, body: RequestBody? = null): String {
        val request = Request.Builder().apply {
            url(url)
            method(method, body)
            headers(headers)
        }.build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            return response.body.string()
        } catch (e: IOException) {
            e.printStackTrace()
            throw TwitterException(e)
        }
    }

}
