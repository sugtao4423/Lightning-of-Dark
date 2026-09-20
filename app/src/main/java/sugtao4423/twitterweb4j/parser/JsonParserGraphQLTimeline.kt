package sugtao4423.twitterweb4j.parser

import org.json.JSONException
import sugtao4423.twitter4j.Status
import sugtao4423.twitter4j.TwitterException
import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.model.CursorList
import sugtao4423.twitterweb4j.parseJson
import sugtao4423.twitterweb4j.parser.model.parseStatus

object JsonParserGraphQLTimeline {

    private val ignoreSource = Regex("Twitter for Advertisers|advertiser-interface")

    @Throws(JSONException::class, TwitterException::class)
    private fun parse(
        instructions: Json,
        entryType: String = "TimelineAddEntries",
        convPrefix: String? = null,
        ignoreMissingCursorTop: Boolean = false,
        ignoreMissingCursorBottom: Boolean = false,
    ): CursorList<Status> {
        val entries = instructions.find { it["type"].string == entryType }?.get("entries")
            ?: throw TwitterException("$entryType instruction not found")

        val result = CursorList<Status>()

        for (entry in entries) {
            val entryId = entry["entryId"].string

            if (entryId.startsWith("tweet-") || entryId.startsWith("notification-")) {
                val tweet = parseStatus(entry["content"]["itemContent"]["tweet_results"]["result"])
                if (tweet != null && !tweet.source.contains(ignoreSource)) {
                    result.add(tweet)
                }
            } else if (convPrefix != null && entryId.startsWith(convPrefix)) {
                for (item in entry["content"]["items"]) {
                    val itemEntryId = item["entryId"].string
                    if (!itemEntryId.contains("-tweet-") || itemEntryId.contains("promoted")) {
                        continue
                    }

                    val tweet = parseStatus(item["item"]["itemContent"]["tweet_results"]["result"])
                    if (tweet != null && !tweet.source.contains(ignoreSource)) {
                        result.add(tweet)
                    }
                }
            } else if (entryId.startsWith("cursor-top-")) {
                if (result.cursorTop != null) {
                    throw TwitterException("cursor-top is already set")
                }
                result.cursorTop = entry["content"]["value"].string
            } else if (entryId.startsWith("cursor-bottom-")) {
                if (result.cursorBottom != null) {
                    throw TwitterException("cursor-bottom is already set")
                }
                result.cursorBottom = entry["content"]["value"].string
            }
        }

        if (!ignoreMissingCursorTop && result.cursorTop == null) {
            throw TwitterException("cursor-top is not set")
        }
        if (!ignoreMissingCursorBottom && result.cursorBottom == null) {
            throw TwitterException("cursor-bottom is not set")
        }

        result.sortDescending()

        return result
    }

    @Throws(TwitterException::class)
    private inline fun <T> parseResponse(response: String, block: (Json) -> T): T = try {
        block(response.parseJson())
    } catch (e: JSONException) {
        throw TwitterException(e)
    }

    @Throws(TwitterException::class)
    fun parseHomeLatestTimeline(response: String): CursorList<Status> = parseResponse(response) {
        val instructions = it["data"]["home"]["home_timeline_urt"]["instructions"]
        parse(instructions, convPrefix = "home-conversation-")
    }

    @Throws(TwitterException::class)
    fun parseMentionsTimeline(response: String): CursorList<Status> = parseResponse(response) {
        val instructions =
            it["data"]["viewer_v2"]["user_results"]["result"]["notification_timeline"]["timeline"]["instructions"]
        parse(instructions, ignoreMissingCursorBottom = true)
    }

    @Throws(TwitterException::class)
    fun parseListTweetsTimeline(response: String): CursorList<Status> = parseResponse(response) {
        val instructions = it["data"]["list"]["tweets_timeline"]["timeline"]["instructions"]
        parse(instructions, convPrefix = "list-conversation-")
    }

    @Throws(TwitterException::class)
    fun parseTweetDetail(response: String, tweetId: Long): Status = parseResponse(response) {
        val instructions = it["data"]["threaded_conversation_with_injections_v2"]["instructions"]
        val conversations = parse(
            instructions,
            convPrefix = "conversationthread-",
            ignoreMissingCursorTop = true,
            ignoreMissingCursorBottom = true,
        )

        conversations.find { status -> status.id == tweetId }
            ?: throw TwitterException("Tweet with ID $tweetId not found in the conversation")
    }

    @Throws(TwitterException::class)
    fun parseUserTweetsAndReplies(response: String, userId: Long): CursorList<Status> =
        parseResponse(response) {
            val instructions =
                it["data"]["user"]["result"]["timeline_v2"]["timeline"]["instructions"]
            val userTimeline = parse(instructions, convPrefix = "profile-conversation-")

            userTimeline.filterTo(userTimeline.newWithSameCursors()) { status ->
                status.user.id == userId
            }
        }

    @Throws(TwitterException::class)
    fun parseLikes(response: String): CursorList<Status> = parseResponse(response) {
        val instructions = it["data"]["user"]["result"]["timeline"]["timeline"]["instructions"]
        parse(instructions)
    }

}
