package sugtao4423.twitterweb4j.body

import org.json.JSONException

class UnfavoriteTweetBody(requestUrl: String) : BaseBody(requestUrl) {

    @Throws(JSONException::class)
    fun get(tweetId: Long): String {
        val variables = mapOf("tweet_id" to tweetId.toString())
        return buildJsonString(variables)
    }

}
