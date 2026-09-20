package sugtao4423.twitterweb4j.body

import okhttp3.HttpUrl
import okhttp3.RequestBody
import sugtao4423.twitterweb4j.model.CreateTweet
import sugtao4423.twitterweb4j.url.UrlGraphQLFeatures

class CreateTweetBody(url: HttpUrl) : BaseBody(url) {

    override val features = UrlGraphQLFeatures.generateMap(additional = true)

    fun get(tweet: CreateTweet): RequestBody {
        val variables = mutableMapOf(
            "dark_request" to false,
            "semantic_annotation_ids" to emptyList<Any>(),
            "tweet_text" to tweet.text,
        )

        tweet.inReplyToStatusId?.let {
            variables["reply"] = mapOf(
                "in_reply_to_tweet_id" to it.toString(),
                "exclude_reply_user_ids" to emptyList<Any>(),
            )
        }
        tweet.attachmentUrl?.let {
            variables["attachment_url"] = it
        }
        variables["media"] = mapOf(
            "media_entities" to tweet.mediaIds.map {
                mapOf("media_id" to it.toString(), "tagged_users" to emptyList<Any>())
            },
            "possibly_sensitive" to false,
        )
        return buildJsonBody(variables)
    }

}
