package sugtao4423.twitterweb4j.body

import sugtao4423.twitterweb4j.url.UrlGraphQLFeatures

class CreateTweetBody(requestUrl: String) : BaseBody(requestUrl) {

    override val features = UrlGraphQLFeatures.generateMap(additional = true)

    override val variables = mapOf(
        "dark_request" to false,
        "semantic_annotation_ids" to emptyList<Any>(),
        "media" to mapOf(
            "media_entities" to emptyList<Any>(),
            "possibly_sensitive" to false,
        ),
    )

    fun get(tweetText: String): String {
        val variables = this.variables + mapOf("tweet_text" to tweetText)
        return buildJsonString(variables)
    }

}
