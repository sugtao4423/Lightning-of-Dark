package sugtao4423.lod.utils

import android.content.Context
import sugtao4423.lod.App
import twitter4j.MediaEntity

class Utils {

    companion object {

        fun getVideoURLsSortByBitrate(app: App, mentitys: Array<MediaEntity>): Array<String> {
            var urls = arrayOf<String?>()
            mentitys.map { media ->
                if (isVideoOrGif(media)) {
                    val videos = arrayListOf<VideoURLs>()
                    media.videoVariants.map {
                        var find = false
                        if (app.getOptions().isWebm && it.contentType == "video/webm") {
                            find = true
                        } else if (!app.getOptions().isWebm && it.contentType == "video/mp4") {
                            find = true
                        }

                        if (find) {
                            videos.add(VideoURLs(it.bitrate, it.url))
                        }
                    }
                    if (videos.isEmpty()) {
                        media.videoVariants.map {
                            var find = false
                            if (it.contentType == "video/mp4") {
                                find = true
                            } else if (it.contentType == "video/webm") {
                                find = true
                            }

                            if (find) {
                                videos.add(VideoURLs(it.bitrate, it.url))
                            }
                        }
                    }
                    videos.sort()
                    urls = arrayOfNulls(videos.size)
                    videos.mapIndexed { index, videoURLs ->
                        urls[index] = videoURLs.url
                    }
                }
            }
            return urls.requireNoNulls()
        }

        data class VideoURLs(
                val bitrate: Int,
                val url: String
        ) : Comparable<VideoURLs> {
            override fun compareTo(other: VideoURLs): Int {
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