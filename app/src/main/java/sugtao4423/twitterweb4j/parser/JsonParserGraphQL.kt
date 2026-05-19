package sugtao4423.twitterweb4j.parser

import org.json.JSONException
import sugtao4423.twitter4j.Status
import sugtao4423.twitter4j.TwitterException
import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.parseJson
import sugtao4423.twitterweb4j.parser.model.parseStatus

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
            return parseStatus(statusJson)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }
    }

    @Throws(TwitterException::class)
    fun parseDeleteTweet(response: String) {
        response.parse()["data"]["delete_tweet"]["tweet_results"].orNull()
            ?: throw TwitterException("Missing 'data.delete_tweet.tweet_results' in response.")
    }

    @Throws(TwitterException::class)
    fun parseCreateRetweet(response: String) {
        response.parse()["data"]["create_retweet"]["retweet_results"]["result"].orNull()
            ?: throw TwitterException("Missing 'data.create_retweet.retweet_results.result' in response.")
    }

    @Throws(TwitterException::class)
    fun parseDeleteRetweet(response: String) {
        response.parse()["data"]["unretweet"]["source_tweet_results"]["result"].orNull()
            ?: throw TwitterException("Missing 'data.unretweet.source_tweet_results.result' in response.")
    }

    @Throws(TwitterException::class)
    fun parseFavoriteTweet(response: String) {
        response.parse()["data"]["favorite_tweet"].stringOrNull
            ?: throw TwitterException("Missing 'data.favorite_tweet' in response.")
    }

    @Throws(TwitterException::class)
    fun parseUnfavoriteTweet(response: String) {
        response.parse()["data"]["unfavorite_tweet"].stringOrNull
            ?: throw TwitterException("Missing 'data.unfavorite_tweet' in response.")
    }

}
