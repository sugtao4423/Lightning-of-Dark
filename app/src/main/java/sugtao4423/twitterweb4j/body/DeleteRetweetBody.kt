package sugtao4423.twitterweb4j.body

class DeleteRetweetBody(requestUrl: String) : BaseBody(requestUrl) {

    override val variables = mapOf(
        "dark_request" to false,
    )

    fun get(tweetId: Long): String {
        val variables = this.variables + mapOf("source_tweet_id" to tweetId.toString())
        return buildJsonString(variables)
    }

}
