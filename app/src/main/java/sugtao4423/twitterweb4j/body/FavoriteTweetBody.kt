package sugtao4423.twitterweb4j.body

class FavoriteTweetBody(requestUrl: String) : BaseBody(requestUrl) {

    fun get(tweetId: Long): String {
        val variables = mapOf("tweet_id" to tweetId.toString())
        return buildJsonString(variables)
    }

}
