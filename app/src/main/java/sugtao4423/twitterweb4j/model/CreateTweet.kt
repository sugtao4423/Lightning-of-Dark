package sugtao4423.twitterweb4j.model

data class CreateTweet(
    val text: String,
    var inReplyToStatusId: Long? = null,
)
