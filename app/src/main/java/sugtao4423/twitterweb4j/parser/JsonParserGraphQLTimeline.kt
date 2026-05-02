package sugtao4423.twitterweb4j.parser

import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.impl.StatusJSONImpl
import sugtao4423.twitterweb4j.model.CursorList
import sugtao4423.twitterweb4j.parseJson
import twitter4j.JSONException
import twitter4j.Status
import twitter4j.TwitterException

object JsonParserGraphQLTimeline {

    private val ignoreSource = Regex("Twitter for Advertisers|advertiser-interface")

    @Throws(JSONException::class, TwitterException::class)
    private fun parse(
        instructions: Json,
        entry: String = "TimelineAddEntries",
        convPrefix: String? = null,
        ignoreMissingCursorTop: Boolean = false,
        ignoreMissingCursorBottom: Boolean = false,
    ): CursorList<Status> {
        val entries = instructions.iterator().asSequence().find {
            it["type"].string == entry
        }?.get("entries") ?: throw TwitterException("$entry instruction not found")

        val result = CursorList<Status>()

        for (entry in entries) {
            val entryId = entry["entryId"].string

            if (entryId.startsWith("tweet-") || entryId.startsWith("notification-")) {
                val tweet =
                    StatusJSONImpl(entry["content"]["itemContent"]["tweet_results"]["result"])
                if (!tweet.source.contains(ignoreSource)) {
                    result.add(tweet)
                }
            } else if (convPrefix != null && entryId.startsWith(convPrefix)) {
                for (item in entry["content"]["items"]) {
                    val itemEntryId = item["entryId"].string
                    if (!itemEntryId.contains("-tweet-") || itemEntryId.contains("promoted")) {
                        continue
                    }

                    val tweet =
                        StatusJSONImpl(item["item"]["itemContent"]["tweet_results"]["result"])
                    if (!tweet.source.contains(ignoreSource)) {
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
    fun parseHomeLatestTimeline(response: String): CursorList<Status> {
        try {
            val instructions =
                response.parseJson()["data"]["home"]["home_timeline_urt"]["instructions"]
            return parse(instructions, convPrefix = "home-conversation-")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseMentionsTimeline(response: String): CursorList<Status> {
        try {
            val instructions =
                response.parseJson()["data"]["viewer_v2"]["user_results"]["result"]["notification_timeline"]["timeline"]["instructions"]
            return parse(instructions, ignoreMissingCursorBottom = true)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseListTweetsTimeline(response: String): CursorList<Status> {
        try {
            val instructions =
                response.parseJson()["data"]["list"]["tweets_timeline"]["timeline"]["instructions"]
            return parse(instructions, convPrefix = "list-conversation-")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseTweetDetail(response: String, tweetId: Long): Status {
        try {
            val instructions =
                response.parseJson()["data"]["threaded_conversation_with_injections_v2"]["instructions"]
            val conversations = parse(
                instructions, convPrefix = "conversationthread-", ignoreMissingCursorTop = true
            )

            return conversations.find { it.id == tweetId }
                ?: throw TwitterException("Tweet with ID $tweetId not found in the conversation")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseUserTweetsAndReplies(response: String, userId: Long): CursorList<Status> {
        try {
            val instructions =
                response.parseJson()["data"]["user"]["result"]["timeline_v2"]["timeline"]["instructions"]
            val userTimeline = parse(instructions, convPrefix = "profile-conversation-")

            return userTimeline.filterTo(CursorList.newWithCursor(userTimeline)) {
                it.isRetweeted || it.user.id == userId
            }
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseLikes(response: String): CursorList<Status> {
        try {
            val instructions =
                response.parseJson()["data"]["user"]["result"]["timeline"]["timeline"]["instructions"]
            return parse(instructions)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

}
