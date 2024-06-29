package sugtao4423.twitterweb4j.parser

import org.json.JSONException
import org.json.JSONObject
import sugtao4423.twitterweb4j.model.Status
import twitter4j.TwitterException

object JsonParserGraphQL {

    @Throws(JSONException::class)
    private fun nestedJsonObject(response: String, vararg nests: String): JSONObject {
        var json = JSONObject(response)
        nests.forEach { json = json.getJSONObject(it) }
        return json
    }

    @Throws(TwitterException::class)
    fun parseCreateTweet(response: String): Status {
        try {
            val statusJson = nestedJsonObject(
                response, "data", "create_tweet", "tweet_results", "result"
            )
            return Status(statusJson)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseDeleteTweet(response: String) {
        try {
            nestedJsonObject(response, "data", "delete_tweet", "tweet_results")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseCreateRetweet(response: String) {
        try {
            nestedJsonObject(
                response, "data", "create_retweet", "retweet_results", "result"
            )
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseDeleteRetweet(response: String) {
        try {
            nestedJsonObject(
                response, "data", "unretweet", "source_tweet_results", "result"
            )
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseFavoriteTweet(response: String) {
        try {
            val data = nestedJsonObject(response, "data")
            data.getString("favorite_tweet")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseUnfavoriteTweet(response: String) {
        try {
            val data = nestedJsonObject(response, "data")
            data.getString("unfavorite_tweet")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

}
