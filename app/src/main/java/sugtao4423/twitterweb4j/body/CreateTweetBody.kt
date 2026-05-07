package sugtao4423.twitterweb4j.body

import sugtao4423.twitterweb4j.model.CreateTweet
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

    fun get(tweet: CreateTweet): String {
        val variables = (this.variables + mapOf("tweet_text" to tweet.text)).toMutableMap()
        tweet.inReplyToStatusId?.let {
            variables["reply"] = mapOf(
                "in_reply_to_tweet_id" to it.toString(),
                "exclude_reply_user_ids" to emptyList<Any>(),
            )
        }
        return buildJsonString(variables)
    }

}
