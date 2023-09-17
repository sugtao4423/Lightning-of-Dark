package sugtao4423.twitterweb4j.body

import org.json.JSONException

object DeleteTweetBody : BaseBody() {

    override val queryId = "VaenaVgh5q5ih7kvyVjgtg"

    override val variables = mapOf(
        "dark_request" to false,
    )

    @Throws(JSONException::class)
    fun get(tweetId: Long): String {
        val variables = this.variables + mapOf("tweet_id" to tweetId.toString())
        return buildJsonString(variables)
    }

}
