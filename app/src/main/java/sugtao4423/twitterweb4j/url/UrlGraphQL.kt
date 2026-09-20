package sugtao4423.twitterweb4j.url

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject

object UrlGraphQL {

    private val apiBaseUrl = "https://twitter.com/i/api/graphql".replace("twitter", "x")

    private fun build(
        path: String,
        variables: Map<String, Any>,
        features: String = UrlGraphQLFeatures.generate(additional = true),
    ): HttpUrl = "$apiBaseUrl/$path".toHttpUrl().newBuilder().apply {
        addQueryParameter("variables", JSONObject(variables).toString())
        addQueryParameter("features", features)
    }.build()

    fun homeLatestTimeline(count: Int, cursor: String? = null): HttpUrl = build(
        "cWF3cqWadLlIXA6KJWhcew/HomeLatestTimeline",
        buildMap {
            put("count", count)
            put("includePromotedContent", false)
            put("latestControlAvailable", true)
            cursor?.let { put("cursor", it) }
        },
    )

    fun mentionsTimeline(count: Int, cursor: String? = null): HttpUrl = build(
        "8bj3MP0KXWKlpfC1yvGfbQ/NotificationsTimeline",
        buildMap {
            put("timeline_type", "Mentions")
            put("count", count)
            cursor?.let { put("cursor", it) }
        },
    )

    fun listTweetsTimeline(listId: Long, count: Int, cursor: String? = null): HttpUrl = build(
        "l411pL-GRg-AKo_a2rmYjg/ListLatestTweetsTimeline",
        buildMap {
            put("listId", listId.toString())
            put("count", count)
            put("includePromotedContent", false)
            cursor?.let { put("cursor", it) }
        },
    )

    fun tweetDetail(tweetId: Long): HttpUrl = build(
        "rU08O-YiXdr0IZfE7qaUMg/TweetDetail",
        mapOf(
            "focalTweetId" to tweetId.toString(),
            "with_rux_injections" to false,
            "includePromotedContent" to false,
            "withCommunity" to true,
            "withQuickPromoteEligibilityTweetFields" to true,
            "withBirdwatchNotes" to true,
            "withVoice" to true,
        ),
    )

    fun userTweetsAndReplies(userId: Long, count: Int, cursor: String? = null): HttpUrl = build(
        "wxoVeDnl0mP7VLhe6mTOdg/UserTweetsAndReplies",
        buildMap {
            put("userId", userId.toString())
            put("count", count)
            put("includePromotedContent", false)
            put("withQuickPromoteEligibilityTweetFields", false)
            put("withVoice", true)
            put("withV2Timeline", true)
            cursor?.let { put("cursor", it) }
        },
    )

    fun likes(userId: Long, count: Int, cursor: String? = null): HttpUrl = build(
        "KPuet6dGbC8LB2sOLx7tZQ/Likes",
        buildMap {
            put("userId", userId.toString())
            put("count", count)
            put("includePromotedContent", false)
            put("withClientEventToken", false)
            put("withBirdwatchNotes", false)
            put("withVoice", true)
            cursor?.let { put("cursor", it) }
        },
    )

    fun following(userId: Long, count: Int, cursor: String? = null): HttpUrl = build(
        "vWCjN9gcTJiXzzMPR5Oxzw/Following",
        buildMap {
            put("userId", userId.toString())
            put("count", count)
            put("includePromotedContent", false)
            cursor?.let { put("cursor", it) }
        },
    )

    fun followers(userId: Long, count: Int, cursor: String? = null): HttpUrl = build(
        "-WcGoRt8IQuPm-l1ymgy6g/Followers",
        buildMap {
            put("userId", userId.toString())
            put("count", count)
            put("includePromotedContent", false)
            cursor?.let { put("cursor", it) }
        },
    )

    val createTweet = "$apiBaseUrl/oB-5XsHNAbjvARJEc8CZFw/CreateTweet".toHttpUrl()
    val deleteTweet = "$apiBaseUrl/VaenaVgh5q5ih7kvyVjgtg/DeleteTweet".toHttpUrl()
    val createRetweet = "$apiBaseUrl/ojPdsZsimiJrUGLR1sjUtA/CreateRetweet".toHttpUrl()
    val deleteRetweet = "$apiBaseUrl/iQtK4dl5hBmXewYZuEOKVw/DeleteRetweet".toHttpUrl()
    val favoriteTweet = "$apiBaseUrl/lI07N6Otwv1PhnEgXILM7A/FavoriteTweet".toHttpUrl()
    val unfavoriteTweet = "$apiBaseUrl/ZYKSe-w7KEslx3JhSIk5LA/UnfavoriteTweet".toHttpUrl()

}
