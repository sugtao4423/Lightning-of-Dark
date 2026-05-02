package sugtao4423.twitterweb4j.parser

import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.impl.UserJSONImpl
import sugtao4423.twitterweb4j.model.PagableCursorList
import sugtao4423.twitterweb4j.parseJson
import twitter4j.JSONException
import twitter4j.TwitterException
import twitter4j.User

object JsonParserGraphQLUser {

    @Throws(JSONException::class, TwitterException::class)
    private fun parse(
        instructions: Json, entry: String = "TimelineAddEntries"
    ): PagableCursorList<User> {
        val entries = instructions.iterator().asSequence().find {
            it["type"].string == entry
        }?.get("entries") ?: throw TwitterException("$entry instruction not found")

        val result = PagableCursorList<User>()

        for (entry in entries) {
            val entryId = entry["entryId"].string

            if (entryId.startsWith("user-")) {
                val user = UserJSONImpl(entry["content"]["itemContent"]["user_results"]["result"])
                result.add(user)
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
            val instructions =
                response.parseJson()["data"]["user"]["result"]["timeline"]["timeline"]["instructions"]
            return parse(instructions)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseFollowers(response: String): PagableCursorList<User> = parseFollowing(response)

}
