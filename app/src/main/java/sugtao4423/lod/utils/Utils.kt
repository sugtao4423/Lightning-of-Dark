package sugtao4423.lod.utils

import android.content.Context
import twitter4j.MediaEntity

class Utils {

    companion object {

        fun getVideoUrlHiBitrate(mediaEntities: Array<MediaEntity>): String? {
            val videos = ArrayList<VideoUrl>()
            mediaEntities.map {
                if (isVideoOrGif(it)) {
                    it.videoVariants.map { variant ->
                        videos.add(VideoUrl(variant.bitrate, variant.url))
                    }
                    videos.sort()
                }
            }

            return if (videos.isEmpty()) {
                null
            } else {
                videos.last().url
            }
        }

        data class VideoUrl(
                val bitrate: Int,
                val url: String
        ) : Comparable<VideoUrl> {
            override fun compareTo(other: VideoUrl): Int {
                return this.bitrate - other.bitrate
            }
        }

        fun isVideoOrGif(ex: MediaEntity): Boolean {
            return (isVideo(ex) || isGif(ex))
        }

        fun isVideo(ex: MediaEntity): Boolean {
            return ex.type == "video"
        }

        fun isGif(ex: MediaEntity): Boolean {
            return ex.type == "animated_gif"
        }

        fun convertDpToPx(context: Context, dp: Int): Int {
            val d = context.resources.displayMetrics.density
            return ((dp * d) + 0.5).toInt()
        }

    }

}