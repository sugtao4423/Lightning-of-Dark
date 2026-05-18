package sugtao4423.twitterweb4j.body

import okhttp3.HttpUrl

class UnfavoriteTweetBody(requestUrl: HttpUrl) : BaseBody(requestUrl) {

    fun get(tweetId: Long): String {
        val variables = mapOf("tweet_id" to tweetId.toString())
        return buildJsonString(variables)
    }

}
