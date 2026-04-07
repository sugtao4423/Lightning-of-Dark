package sugtao4423.twitterweb4j.parser

import sugtao4423.twitterweb4j.impl.StatusJSONImpl
import sugtao4423.twitterweb4j.model.CursorList
import twitter4j.JSONArray
import twitter4j.JSONException
import twitter4j.JSONObject
import twitter4j.Status
import twitter4j.TwitterException

object JsonParserGraphQLTimeline {

    @Throws(JSONException::class)
    private fun JSONObject.nestedJSONObject(vararg nests: String): JSONObject {
        var json = this
        nests.forEach { json = json.getJSONObject(it) }
        return json
    }

    private val ignoreSource = Regex("Twitter for Advertisers|advertiser-interface")

    @Throws(JSONException::class, TwitterException::class)
    private fun parse(
        instructions: JSONArray, entry: String = "TimelineAddEntries", convPrefix: String? = null
    ): CursorList<Status> {
        val instructionObjects = (0 until instructions.length()).map {
            instructions.getJSONObject(it)
        }
        val entries = instructionObjects.find {
            it.getString("type") == entry
        }?.getJSONArray("entries") ?: throw TwitterException("$entry instruction not found")

        val result = CursorList<Status>()

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val entryId = entry.getString("entryId")

            if (entryId.startsWith("tweet-") || entryId.startsWith("notification-")) {
                val tweet = StatusJSONImpl(
                    entry.nestedJSONObject(
                        "content", "itemContent", "tweet_results", "result"
                    )
                )

                if (!tweet.source.contains(ignoreSource)) {
                    result.add(tweet)
                }
            } else if (convPrefix != null && entryId.startsWith(convPrefix)) {
                val content = entry.getJSONObject("content")
                val items = content.getJSONArray("items")

                for (j in 0 until items.length()) {
                    val item = items.getJSONObject(j)
                    val itemEntryId = item.getString("entryId")

                    if (!itemEntryId.contains("-tweet-") || itemEntryId.contains("promoted")) {
                        continue
                    }

                    val tweet = StatusJSONImpl(
                        item.nestedJSONObject(
                            "item", "itemContent", "tweet_results", "result"
                        )
                    )

                    if (!tweet.source.contains(ignoreSource)) {
                        result.add(tweet)
                    }
                }
            } else if (entryId.startsWith("cursor-top-")) {
                if (result.cursorTop != null) {
                    throw TwitterException("cursor-top is already set")
                }

                val value = entry.getJSONObject("content").getString("value")
                result.cursorTop = value
            } else if (entryId.startsWith("cursor-bottom-")) {
                if (result.cursorBottom != null) {
                    throw TwitterException("cursor-bottom is already set")
                }

                val value = entry.getJSONObject("content").getString("value")
                result.cursorBottom = value
            }
        }

        if (result.cursorTop == null) {
            throw TwitterException("cursor-top is not set")
        }
        if (result.cursorBottom == null) {
            throw TwitterException("cursor-bottom is not set")
        }

        result.sortDescending()

        return result
    }

    @Throws(TwitterException::class)
    fun parseHomeLatestTimeline(response: String): CursorList<Status> {
        try {
            val instructions = JSONObject(response).nestedJSONObject(
                "data", "home", "home_timeline_urt"
            ).getJSONArray("instructions")
            return parse(instructions, convPrefix = "home-conversation-")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseMentionsTimeline(response: String): CursorList<Status> {
        try {
            val instructions = JSONObject(response).nestedJSONObject(
                "data", "viewer_v2", "user_results", "result", "notification_timeline", "timeline"
            ).getJSONArray("instructions")
            return parse(instructions)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseListTweetsTimeline(response: String): CursorList<Status> {
        try {
            val instructions = JSONObject(response).nestedJSONObject(
                "data", "list", "tweets_timeline", "timeline"
            ).getJSONArray("instructions")
            return parse(instructions, convPrefix = "list-conversation-")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseUserTweetsAndReplies(response: String, userId: Long): CursorList<Status> {
        try {
            val instructions = JSONObject(response).nestedJSONObject(
                "data", "user", "result", "timeline_v2", "timeline"
            ).getJSONArray("instructions")
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
            val instructions = JSONObject(response).nestedJSONObject(
                "data", "user", "result", "timeline", "timeline"
            ).getJSONArray("instructions")
            return parse(instructions)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

}
