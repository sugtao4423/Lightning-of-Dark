package sugtao4423.lod.ui.adapter.converter

import twitter4j.MediaEntity
import twitter4j.Status
import java.text.SimpleDateFormat
import java.util.*

object TweetViewDataConverter {

    @JvmStatic
    fun originalStatus(status: Status?): Status? = status?.let {
        if (it.isRetweet) it.retweetedStatus else it
    }

    @JvmStatic
    fun isShowProtected(status: Status?): Boolean =
        originalStatus(status)?.user?.isProtected ?: false

    @JvmStatic
    fun isShowRetweetUser(status: Status?): Boolean = status?.isRetweet ?: false

    @JvmStatic
    fun userIconUrl(status: Status?): String? =
        originalStatus(status)?.user?.biggerProfileImageURLHttps

    @JvmStatic
    fun userNameAndScreenName(status: Status?): String? = originalStatus(status)?.let {
        "${it.user.name} - @${it.user.screenName}"
    }

    @JvmStatic
    fun date(status: Status?, isShowMilliSec: Boolean): String? = status?.let {
        val statusDateFormat = SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss" + (if (isShowMilliSec) ".SSS" else ""),
            Locale.getDefault()
        )
        val date =
            statusDateFormat.format(Date((originalStatus(status)!!.id shr 22) + 1288834974657L))
        if (status.isRetweet) {
            "$date  Retweeted by "
        } else {
            val via = status.source.replace(Regex("<.+?>"), "")
            "$date  via $via"
        }
    }

    @JvmStatic
    fun retweetedUserIconUrl(status: Status?): String? =
        if (status?.isRetweet == true) status.user.biggerProfileImageURLHttps else null

    @JvmStatic
    fun retweetedUserScreenName(status: Status?): String? =
        if (status?.isRetweet == true) "@${status.user.screenName}" else null

    @JvmStatic
    fun text(status: Status?): String? = originalStatus(status)?.text

    @JvmStatic
    fun isShowMediaList(status: Status?) = status?.mediaEntities?.isNotEmpty() ?: false

    @JvmStatic
    fun allImageUrls(mediaEntities: List<MediaEntity>): List<String> =
        mediaEntities.filter { !mediaIsVideoOrGif(it) }.map { it.mediaURLHttps }

    @JvmStatic
    fun mediaIsVideoOrGif(mediaEntity: MediaEntity): Boolean =
        mediaEntity.type == "video" || mediaEntity.type == "animated_gif"

    @JvmStatic
    fun mediaIsGif(mediaEntity: MediaEntity): Boolean = mediaEntity.type == "animated_gif"

    @JvmStatic
    fun mediaThumbnailUrl(mediaEntity: MediaEntity): String = mediaEntity.mediaURLHttps + ":small"

    @JvmStatic
    fun videoMediaUrl(mediaEntity: MediaEntity): String {
        if (!mediaIsVideoOrGif(mediaEntity)) {
            throw UnsupportedOperationException()
        }

        val videoVariants = mediaEntity.videoVariants.map { Pair(it.bitrate, it.url) }
        if (videoVariants.isEmpty()) {
            throw Error()
        }

        return videoVariants.maxByOrNull { it.first }!!.second
    }

}
