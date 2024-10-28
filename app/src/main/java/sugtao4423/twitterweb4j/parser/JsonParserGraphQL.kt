package sugtao4423.twitterweb4j.parser

import sugtao4423.twitterweb4j.impl.StatusJSONImpl
import twitter4j.JSONException
import twitter4j.JSONObject
import twitter4j.Status
import twitter4j.TwitterException

object JsonParserGraphQL {

    @Throws(TwitterException::class)
    private fun String.getJsonObject(vararg nests: String): JSONObject {
        try {
            var json = JSONObject(this)
            if (json.has("errors")) {
                throw TwitterException(this)
            }
            nests.forEach { json = json.getJSONObject(it) }
            return json
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseCreateTweet(response: String): Status {
        val statusJson = response.getJsonObject("data", "create_tweet", "tweet_results", "result")
        try {
            return StatusJSONImpl(statusJson)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseDeleteTweet(response: String) {
        response.getJsonObject("data", "delete_tweet", "tweet_results")
    }

    @Throws(TwitterException::class)
    fun parseCreateRetweet(response: String) {
        response.getJsonObject("data", "create_retweet", "retweet_results", "result")
    }

    @Throws(TwitterException::class)
    fun parseDeleteRetweet(response: String) {
        response.getJsonObject("data", "unretweet", "source_tweet_results", "result")
    }

    @Throws(TwitterException::class)
    fun parseFavoriteTweet(response: String) {
        val data = response.getJsonObject("data")
        try {
            data.getString("favorite_tweet")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseUnfavoriteTweet(response: String) {
        val data = response.getJsonObject("data")
        try {
            data.getString("unfavorite_tweet")
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

}
