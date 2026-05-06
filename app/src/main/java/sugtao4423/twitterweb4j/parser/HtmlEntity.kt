package sugtao4423.twitterweb4j.parser

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

    private val NAMED_ENTITIES = mapOf(
        "amp" to "&",
        "lt" to "<",
        "gt" to ">",
    )

    private fun decodeEntity(token: String): String? = when {
        token.startsWith("#x", ignoreCase = true) ->
            token.drop(2).toIntOrNull(16)?.let { String(Character.toChars(it)) }

        token.startsWith("#") ->
            token.drop(1).toIntOrNull()?.let { String(Character.toChars(it)) }

        else -> NAMED_ENTITIES[token]
    }

    fun unescapeAndSlideEntityIndices(
        text: String,
        userMentionEntities: Array<UserMentionEntityJSONImpl>,
        urlEntities: Array<URLEntityJSONImpl>,
        hashtagEntities: Array<HashtagEntityJSONImpl>,
        mediaEntities: Array<MediaEntityJSONImpl>,
    ): UnescapedTweet {
        data class Slot(val typeId: Int, val idx: Int, val origStart: Int, val origEnd: Int)

        val newStarts = arrayOf(
            IntArray(userMentionEntities.size), IntArray(urlEntities.size),
            IntArray(hashtagEntities.size), IntArray(mediaEntities.size),
        )
        val newEnds = arrayOf(
            IntArray(userMentionEntities.size), IntArray(urlEntities.size),
            IntArray(hashtagEntities.size), IntArray(mediaEntities.size),
        )

        val slots = buildList {
            userMentionEntities.forEachIndexed { i, e -> add(Slot(0, i, e.start, e.end)) }
            urlEntities.forEachIndexed { i, e -> add(Slot(1, i, e.start, e.end)) }
            hashtagEntities.forEachIndexed { i, e -> add(Slot(2, i, e.start, e.end)) }
            mediaEntities.forEachIndexed { i, e -> add(Slot(3, i, e.start, e.end)) }
        }.sortedWith(compareBy({ it.origStart }, { it.origEnd }))

        val out = StringBuilder(text.length)
        var ptr = 0
        var awaitingEnd = false
        var cpIdx = 0

        fun applyBoundaries() {
            while (ptr < slots.size) {
                val s = slots[ptr]
                when {
                    !awaitingEnd && s.origStart == cpIdx -> {
                        newStarts[s.typeId][s.idx] = out.length
                        awaitingEnd = true
                    }

                    awaitingEnd && s.origEnd == cpIdx -> {
                        newEnds[s.typeId][s.idx] = out.length
                        awaitingEnd = false
                        ptr++
                    }

                    else -> return
                }
            }
        }

        var i = 0
        while (i < text.length) {
            applyBoundaries()

            if (text[i] == '&') {
                val semi = text.indexOf(';', i)
                if (semi != -1) {
                    val decoded = decodeEntity(text.substring(i + 1, semi))
                    if (decoded != null) {
                        out.append(decoded)
                        cpIdx += semi - i + 1
                        i = semi + 1
                        continue
                    }
                }
            }

            val cp = text.codePointAt(i)
            out.appendCodePoint(cp)
            cpIdx++
            i += Character.charCount(cp)
        }
        applyBoundaries()

        return UnescapedTweet(
            out.toString(),
            userMentionEntities.mapIndexed { k, e ->
                e.copy(overrideIndices = EntityIndex(newStarts[0][k], newEnds[0][k]))
            }.toTypedArray(),
            urlEntities.mapIndexed { k, e ->
                e.copy(overrideIndices = EntityIndex(newStarts[1][k], newEnds[1][k]))
            }.toTypedArray(),
            hashtagEntities.mapIndexed { k, e ->
                e.copy(overrideIndices = EntityIndex(newStarts[2][k], newEnds[2][k]))
            }.toTypedArray(),
            mediaEntities.mapIndexed { k, e ->
                e.copy(overrideIndices = EntityIndex(newStarts[3][k], newEnds[3][k]))
            }.toTypedArray(),
        )
    }

}
