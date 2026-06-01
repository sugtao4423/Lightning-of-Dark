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
        mediaEntities: List<MediaEntity> = listOf(),
    ): UnescapedTweet {
        val unescapedText = unescape(text)
        val extractor = Extractor()
        val extractedMentions = extractor.extractMentionsOrListsWithIndices(unescapedText)
        val extractedUrls = extractor.extractURLsWithIndices(unescapedText)
        val extractedHashtags = extractor.extractHashtagsWithIndices(unescapedText)

        val userMentions = userMentionEntities.slideIndices(
            extractedMentions, { it.screenName },
        ) { entity, start, end -> entity.copy(start = start, end = end) }

        val urls = urlEntities.slideIndices(
            extractedUrls, { it.url },
        ) { entity, start, end -> entity.copy(start = start, end = end) }

        val hashtags = hashtagEntities.slideIndices(
            extractedHashtags, { it.text },
        ) { entity, start, end -> entity.copy(start = start, end = end) }

        val media = mediaEntities.slideIndices(
            extractedUrls, { it.url },
        ) { entity, start, end -> entity.copy(start = start, end = end) }

        return UnescapedTweet(unescapedText, userMentions, urls, hashtags, media)
    }

    private fun <E> List<E>.slideIndices(
        extracted: List<Extractor.Entity>,
        valueOf: (E) -> String,
        withIndices: (entity: E, start: Int, end: Int) -> E,
    ): List<E> {
        val occurrences = HashMap<String, ArrayDeque<Pair<Int, Int>>>()
        extracted.forEach {
            occurrences.getOrPut(it.value) { ArrayDeque() }.add(it.start to it.end)
        }
        return map { entity ->
            occurrences[valueOf(entity)]?.removeFirstOrNull()
                ?.let { withIndices(entity, it.first, it.second) }
                ?: entity
        }
    }

}
