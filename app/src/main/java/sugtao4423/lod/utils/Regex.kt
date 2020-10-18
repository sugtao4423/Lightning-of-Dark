package sugtao4423.lod.utils

import java.util.regex.Pattern

class Regex {

    companion object {
        val media_image: Pattern = Pattern.compile("http(s)?://pbs.twimg.com/media/")
        val media_video: Pattern = Pattern.compile("http(s)?://video.twimg.com/ext_tw_video/[0-9]+/(pu|pr)/vid/.+/.+(.mp4|.webm)")
        val media_gif: Pattern = Pattern.compile("http(s)?://pbs.twimg.com/tweet_video/")
        val statusUrl: Pattern = Pattern.compile("http(s)?://(mobile.)?twitter.com/(i/web|[0-9a-zA-Z_]+)/status/([0-9]+)")
        const val statusUrlStatusIdGroup = 4
        val shareUrl: Pattern = Pattern.compile("http(s)?://(mobile.)?twitter.com/(intent/tweet|share)\\?.+")
        val userUrl: Pattern = Pattern.compile("http(s)?://(mobile.)?twitter.com/([0-9a-zA-Z_]+)")
        const val userUrlScreenNameGroup = 3
        val twimgUrl: Pattern = Pattern.compile("^http(s)?://pbs.twimg.com/.+/+(.+)(\\..+)$")
        const val twimgUrlFileNameGroup = 2
        const val twimgUrlDotExtGroup = 3
        val userBannerUrl: Pattern = Pattern.compile("^http(s)?://pbs.twimg.com/profile_banners/[0-9]+/([0-9]+)/")
        const val userBannerUrlFileNameGroup = 2
        val userAndAnyUrl: Pattern = Pattern.compile("@[0-9a-zA-Z_]+|http(s)?://[\\w.\\-/:#?=&;%~+]+", Pattern.DOTALL)
    }

}