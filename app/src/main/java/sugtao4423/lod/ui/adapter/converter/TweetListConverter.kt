package sugtao4423.lod.ui.adapter.converter

import sugtao4423.twitter4j.MediaEntity
import sugtao4423.twitter4j.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TweetListConverter {

    fun originalStatus(status: Status): Status = status.let {
        if (it.isRetweet) it.retweetedStatus!! else it
    }

    fun isShowProtected(status: Status): Boolean = originalStatus(status).user.isProtected
    fun isShowRetweetUser(status: Status): Boolean = status.isRetweet

    fun userIconUrl(status: Status): String? = originalStatus(status).user.profileImage?.biggerUrl

    fun userNameAndScreenName(status: Status): String = originalStatus(status).let {
        "${it.user.name} - @${it.user.screenName}"
    }

    fun date(status: Status, isShowMilliSec: Boolean): String {
        val statusDateFormat = SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss" + (if (isShowMilliSec) ".SSS" else ""),
            Locale.getDefault()
        )
        val date =
            statusDateFormat.format(Date((originalStatus(status).id shr 22) + 1288834974657L))
        return if (status.isRetweet) {
            "$date  Retweeted by "
        } else {
            val via = status.source.replace(Regex("<.+?>"), "")
            "$date  via $via"
        }
    }

    fun retweetedUserIconUrl(status: Status): String? =
        if (status.isRetweet) status.user.profileImage?.biggerUrl else null

    fun retweetedUserScreenName(status: Status): String? =
        if (status.isRetweet) "@${status.user.screenName}" else null

    fun text(status: Status): String = originalStatus(status).text

    fun isShowMediaList(status: Status): Boolean = originalStatus(status).mediaEntities.isNotEmpty()

    fun allImageUrls(mediaEntities: List<MediaEntity>): List<String> =
        mediaEntities.filter { !mediaIsVideoOrGif(it) }.map { it.mediaUrl }

    fun mediaIsVideoOrGif(mediaEntity: MediaEntity): Boolean =
        mediaEntity.type == "video" || mediaEntity.type == "animated_gif"

    fun mediaIsGif(mediaEntity: MediaEntity): Boolean = mediaEntity.type == "animated_gif"

    fun mediaThumbnailUrl(mediaEntity: MediaEntity): String = mediaEntity.mediaUrl + ":small"

    fun videoMediaUrl(mediaEntity: MediaEntity): String {
        if (!mediaIsVideoOrGif(mediaEntity)) {
            throw UnsupportedOperationException("Media is not video or gif.")
        }
        if (mediaEntity.videoInfo == null || mediaEntity.videoInfo.variants.isEmpty()) {
            throw UnsupportedOperationException("Video info is not available.")
        }

        return mediaEntity.videoInfo.variants.maxBy { it.bitrate }.url
    }

}
