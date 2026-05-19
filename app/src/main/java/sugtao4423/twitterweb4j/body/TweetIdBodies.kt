package sugtao4423.twitterweb4j.body

import okhttp3.HttpUrl

open class SimpleTweetIdBody(
    requestUrl: HttpUrl,
    private val idKey: String,
    private val includeDarkRequest: Boolean = true,
) : BaseBody(requestUrl) {

    fun get(tweetId: Long): String = buildJsonString(buildMap {
        put(idKey, tweetId.toString())
        if (includeDarkRequest) put("dark_request", false)
    })

}

class DeleteTweetBody(url: HttpUrl) : SimpleTweetIdBody(url, "tweet_id")
class CreateRetweetBody(url: HttpUrl) : SimpleTweetIdBody(url, "tweet_id")
class DeleteRetweetBody(url: HttpUrl) : SimpleTweetIdBody(url, "source_tweet_id")
class FavoriteTweetBody(url: HttpUrl) : SimpleTweetIdBody(url, "tweet_id", false)
class UnfavoriteTweetBody(url: HttpUrl) : SimpleTweetIdBody(url, "tweet_id", false)
