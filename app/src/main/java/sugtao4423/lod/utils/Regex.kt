package sugtao4423.lod.utils

import java.util.regex.Pattern

class Regex {

    companion object {
        val mediaImage: Pattern = Pattern.compile("https?://pbs.twimg.com/media/")
        val mediaVideo: Pattern = Pattern.compile("https?://video.twimg.com/ext_tw_video/[0-9]+/(pu|pr)/vid/.+/.+(.mp4|.webm)")
        val mediaGif: Pattern = Pattern.compile("https?://pbs.twimg.com/tweet_video/")
        val statusUrl: Pattern = Pattern.compile("https?://(mobile.)?twitter.com/(i/web|[0-9a-zA-Z_]+)/status/([0-9]+)")
        const val statusUrlStatusIdGroup = 3
        val shareUrl: Pattern = Pattern.compile("https?://(mobile.)?twitter.com/(intent/tweet|share)\\?.+")
        val userUrl: Pattern = Pattern.compile("https?://(mobile.)?twitter.com/([0-9a-zA-Z_]+)")
        const val userUrlScreenNameGroup = 2
        val twimgUrl: Pattern = Pattern.compile("^https?://pbs.twimg.com/.+/+(.+)(\\..+)$")
        const val twimgUrlFileNameGroup = 1
        const val twimgUrlDotExtGroup = 2
        val userBannerUrl: Pattern = Pattern.compile("^https?://pbs.twimg.com/profile_banners/[0-9]+/([0-9]+)/")
        const val userBannerUrlFileNameGroup = 1
        val userAndAnyUrl: Pattern = Pattern.compile("@[0-9a-zA-Z_]+|https?://[\\w.\\-/:#?=&;%~+]+", Pattern.DOTALL)
    }

}