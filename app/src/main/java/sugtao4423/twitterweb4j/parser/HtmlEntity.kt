package sugtao4423.twitterweb4j.parser

import com.twitter.twittertext.Extractor
import sugtao4423.twitterweb4j.impl.HashtagEntityJSONImpl
import sugtao4423.twitterweb4j.impl.MediaEntityJSONImpl
import sugtao4423.twitterweb4j.impl.URLEntityJSONImpl
import sugtao4423.twitterweb4j.impl.UserMentionEntityJSONImpl
import sugtao4423.twitterweb4j.model.EntityIndex
import twitter4j.HashtagEntity
import twitter4j.MediaEntity
import twitter4j.URLEntity
import twitter4j.UserMentionEntity

object HtmlEntity {

    data class UnescapedTweet(
        val text: String,
        val userMentions: List<UserMentionEntity>,
        val urls: List<URLEntity>,
        val hashtags: List<HashtagEntity>,
        val media: List<MediaEntity>,
    ) : java.io.Serializable

    fun unescapeAndSlideEntityIndices(
        text: String,
        userMentionEntities: List<UserMentionEntityJSONImpl>,
        urlEntities: List<URLEntityJSONImpl>,
        hashtagEntities: List<HashtagEntityJSONImpl>,
        mediaEntities: List<MediaEntityJSONImpl>,
    ): UnescapedTweet {
        val unescapedText = text
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")

        val entities = Extractor().extractEntitiesWithIndices(unescapedText).associate {
            it.value to EntityIndex(it.start, it.end)
        }

        val userMentions = userMentionEntities.map { it.copy(overrideIndices = entities[it.text]) }
        val urls = urlEntities.map { it.copy(overrideIndices = entities[it.text]) }
        val hashtags = hashtagEntities.map { it.copy(overrideIndices = entities[it.text]) }
        val media = mediaEntities.map { it.copy(overrideIndices = entities[it.text]) }

        return UnescapedTweet(unescapedText, userMentions, urls, hashtags, media)
    }

}
