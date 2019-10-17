package sugtao4423.lod.utils

import android.content.Context
import sugtao4423.lod.App
import twitter4j.MediaEntity

class Utils {

    companion object {

        fun getVideoUrlHiBitrate(app: App, mediaEntities: Array<MediaEntity>): String? {
            val mp4 = ArrayList<VideoUrl>()
            val webm = ArrayList<VideoUrl>()
            mediaEntities.map {
                if (isVideoOrGif(it)) {
                    it.videoVariants.map { variant ->
                        val videoUrl = VideoUrl(variant.bitrate, variant.url)
                        when (variant.contentType) {
                            "video/mp4" -> mp4.add(videoUrl)
                            "video/webm" -> webm.add(videoUrl)
                            else -> false
                        }
                    }
                    mp4.sort()
                    webm.sort()
                }
            }

            return when {
                app.getOptions().isWebm && webm.isNotEmpty() -> webm.last().url
                !app.getOptions().isWebm && mp4.isNotEmpty() -> mp4.last().url
                mp4.isNotEmpty() -> mp4.last().url
                webm.isNotEmpty() -> webm.last().url
                else -> null
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