package sugtao4423.twitterweb4j.parser

import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.impl.StatusJSONImpl
import sugtao4423.twitterweb4j.parseJson
import twitter4j.JSONException
import twitter4j.Status
import twitter4j.TwitterException

object JsonParserGraphQL {

    @Throws(TwitterException::class)
    private fun String.parse(): Json = runCatching {
        val json = this.parseJson()
        if (!json["errors"].isNull) {
            throw TwitterException(this)
        }
        json
    }.getOrElse {
        throw TwitterException(it.message, it.cause)
    }

    @Throws(TwitterException::class)
    fun parseCreateTweet(response: String): Status {
        val statusJson = response.parse()["data"]["create_tweet"]["tweet_results"]["result"]
        try {
            return StatusJSONImpl(statusJson)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseDeleteTweet(response: String) {
        response.parse()["data"]["delete_tweet"]["tweet_results"]
    }

    @Throws(TwitterException::class)
    fun parseCreateRetweet(response: String) {
        response.parse()["data"]["create_retweet"]["retweet_results"]["result"]
    }

    @Throws(TwitterException::class)
    fun parseDeleteRetweet(response: String) {
        response.parse()["data"]["unretweet"]["source_tweet_results"]["result"]
    }

    @Throws(TwitterException::class)
    fun parseFavoriteTweet(response: String) {
        val data = response.parse()["data"]
        try {
            data["favorite_tweet"].string
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseUnfavoriteTweet(response: String) {
        val data = response.parse()["data"]
        try {
            data["unfavorite_tweet"].string
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

}
