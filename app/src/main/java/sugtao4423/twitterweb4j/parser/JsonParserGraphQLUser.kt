package sugtao4423.twitterweb4j.parser

import sugtao4423.twitterweb4j.impl.UserJSONImpl
import sugtao4423.twitterweb4j.model.PagableCursorList
import twitter4j.JSONArray
import twitter4j.JSONException
import twitter4j.JSONObject
import twitter4j.TwitterException
import twitter4j.User

object JsonParserGraphQLUser {

    @Throws(JSONException::class)
    private fun JSONObject.nestedJSONObject(vararg nests: String): JSONObject {
        var json = this
        nests.forEach { json = json.getJSONObject(it) }
        return json
    }

    @Throws(JSONException::class, TwitterException::class)
    private fun parse(
        instructions: JSONArray, entry: String = "TimelineAddEntries"
    ): PagableCursorList<User> {
        val instructionObjects = (0 until instructions.length()).map {
            instructions.getJSONObject(it)
        }
        val entries = instructionObjects.find {
            it.getString("type") == entry
        }?.getJSONArray("entries") ?: throw TwitterException("$entry instruction not found")

        val result = PagableCursorList<User>()

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val entryId = entry.getString("entryId")

            if (entryId.startsWith("user-")) {
                val user = UserJSONImpl(
                    entry.nestedJSONObject(
                        "content", "itemContent", "user_results", "result"
                    )
                )
                result.add(user)
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

        return result
    }

    @Throws(TwitterException::class)
    fun parseFollowing(response: String): PagableCursorList<User> {
        try {
            val instructions = JSONObject(response).nestedJSONObject(
                "data", "user", "result", "timeline", "timeline"
            ).getJSONArray("instructions")
            return parse(instructions)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseFollowers(response: String): PagableCursorList<User> = parseFollowing(response)

}
