package sugtao4423.twitterweb4j.media

import sugtao4423.twitter4j.TwitterException
import sugtao4423.twitterweb4j.parseJson

internal object UploadJsonParser {

    data class ProcessingInfo(
        val state: String,
        val checkAfterSecs: Int?,
    ) {
        companion object {
            const val STATE_PENDING = "pending"
            const val STATE_IN_PROGRESS = "in_progress"
            const val STATE_SUCCEEDED = "succeeded"
            const val STATE_FAILED = "failed"
        }
    }

    @Throws(TwitterException::class)
    fun parseUploadMedia(response: String): Long = runCatching {
        response.parseJson()["media_id_string"].string.toLong()
    }.getOrElse {
        throw TwitterException(it.message, it.cause)
    }

    @Throws(TwitterException::class)
    fun parseProcessingInfo(response: String): ProcessingInfo? = runCatching {
        val info = response.parseJson()["processing_info"].orNull() ?: return null

        val state = info["state"].string
        val checkAfterSecs = info["check_after_secs"].intOrNull
        ProcessingInfo(state, checkAfterSecs)
    }.getOrElse {
        throw TwitterException(it.message, it.cause)
    }

}
