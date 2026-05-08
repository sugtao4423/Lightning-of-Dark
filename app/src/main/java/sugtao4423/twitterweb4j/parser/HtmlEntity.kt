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
        val userMentions: Array<UserMentionEntity>,
        val urls: Array<URLEntity>,
        val hashtags: Array<HashtagEntity>,
        val media: Array<MediaEntity>,
    ) : java.io.Serializable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UnescapedTweet
            return text == other.text &&
                    userMentions.contentEquals(other.userMentions) &&
                    urls.contentEquals(other.urls) &&
                    hashtags.contentEquals(other.hashtags) &&
                    media.contentEquals(other.media)
        }

        override fun hashCode(): Int {
            var result = text.hashCode()
            result = 31 * result + userMentions.contentHashCode()
            result = 31 * result + urls.contentHashCode()
            result = 31 * result + hashtags.contentHashCode()
            result = 31 * result + media.contentHashCode()
            return result
        }
    }

    fun unescapeAndSlideEntityIndices(
        text: String,
        userMentionEntities: Array<UserMentionEntityJSONImpl>,
        urlEntities: Array<URLEntityJSONImpl>,
        hashtagEntities: Array<HashtagEntityJSONImpl>,
        mediaEntities: Array<MediaEntityJSONImpl>,
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

        return UnescapedTweet(
            unescapedText,
            userMentions.toTypedArray(),
            urls.toTypedArray(),
            hashtags.toTypedArray(),
            media.toTypedArray(),
        )
    }

}
