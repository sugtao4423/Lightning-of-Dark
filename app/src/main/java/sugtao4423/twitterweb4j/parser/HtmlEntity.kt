package sugtao4423.twitterweb4j.parser

import com.twitter.twittertext.Extractor
import sugtao4423.twitter4j.HashtagEntity
import sugtao4423.twitter4j.MediaEntity
import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.UserMentionEntity

object HtmlEntity {

    fun unescape(text: String): String = text
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")

    data class UnescapedTweet(
        val text: String,
        val userMentions: List<UserMentionEntity>,
        val urls: List<UrlEntity>,
        val hashtags: List<HashtagEntity>,
        val media: List<MediaEntity>,
    ) : java.io.Serializable

    fun unescapeAndSlideEntityIndices(
        text: String,
        userMentionEntities: List<UserMentionEntity>,
        urlEntities: List<UrlEntity>,
        hashtagEntities: List<HashtagEntity>,
        mediaEntities: List<MediaEntity>,
    ): UnescapedTweet {
        val unescapedText = unescape(text)
        val entities = Extractor().extractEntitiesWithIndices(unescapedText).associate {
            it.value to Pair(it.start, it.end)
        }

        val userMentions = userMentionEntities.map {
            it.copy(
                start = entities[it.screenName]?.first ?: it.start,
                end = entities[it.screenName]?.second ?: it.end,
            )
        }
        val urls = urlEntities.map {
            it.copy(
                start = entities[it.url]?.first ?: it.start,
                end = entities[it.url]?.second ?: it.end,
            )
        }
        val hashtags = hashtagEntities.map {
            it.copy(
                start = entities[it.text]?.first ?: it.start,
                end = entities[it.text]?.second ?: it.end,
            )
        }
        val media = mediaEntities.map {
            it.copy(
                start = entities[it.url]?.first ?: it.start,
                end = entities[it.url]?.second ?: it.end,
            )
        }

        return UnescapedTweet(unescapedText, userMentions, urls, hashtags, media)
    }

}
