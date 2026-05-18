package sugtao4423.twitterweb4j.url

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject

object UrlGraphQL {

    private val apiBaseUrl = "https://twitter.com/i/api/graphql".replace("twitter", "x")

    fun homeLatestTimeline(count: Int, cursor: String? = null): HttpUrl {
        val url = "$apiBaseUrl/cWF3cqWadLlIXA6KJWhcew/HomeLatestTimeline"

        val variables = JSONObject().also { json ->
            json.put("count", count)
            json.put("includePromotedContent", false)
            json.put("latestControlAvailable", true)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    fun mentionsTimeline(count: Int, cursor: String? = null): HttpUrl {
        val url = "$apiBaseUrl/8bj3MP0KXWKlpfC1yvGfbQ/NotificationsTimeline"

        val variables = JSONObject().also { json ->
            json.put("timeline_type", "Mentions")
            json.put("count", count)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    fun listTweetsTimeline(listId: Long, count: Int, cursor: String? = null): HttpUrl {
        val url = "$apiBaseUrl/l411pL-GRg-AKo_a2rmYjg/ListLatestTweetsTimeline"

        val variables = JSONObject().also { json ->
            json.put("listId", listId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    fun tweetDetail(tweetId: Long): HttpUrl {
        val url = "$apiBaseUrl/rU08O-YiXdr0IZfE7qaUMg/TweetDetail"

        val variables = JSONObject().also { json ->
            json.put("focalTweetId", tweetId.toString())
            json.put("with_rux_injections", false)
            json.put("includePromotedContent", false)
            json.put("withCommunity", true)
            json.put("withQuickPromoteEligibilityTweetFields", true)
            json.put("withBirdwatchNotes", true)
            json.put("withVoice", true)
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    fun userTweetsAndReplies(userId: Long, count: Int, cursor: String? = null): HttpUrl {
        val url = "$apiBaseUrl/wxoVeDnl0mP7VLhe6mTOdg/UserTweetsAndReplies"

        val variables = JSONObject().also { json ->
            json.put("userId", userId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            json.put("withQuickPromoteEligibilityTweetFields", false)
            json.put("withVoice", true)
            json.put("withV2Timeline", true)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    fun likes(userId: Long, count: Int, cursor: String? = null): HttpUrl {
        val url = "$apiBaseUrl/KPuet6dGbC8LB2sOLx7tZQ/Likes"

        val variables = JSONObject().also { json ->
            json.put("userId", userId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            json.put("withClientEventToken", false)
            json.put("withBirdwatchNotes", false)
            json.put("withVoice", true)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    fun following(userId: Long, count: Int, cursor: String? = null): HttpUrl {
        val url = "$apiBaseUrl/vWCjN9gcTJiXzzMPR5Oxzw/Following"

        val variables = JSONObject().also { json ->
            json.put("userId", userId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    fun followers(userId: Long, count: Int, cursor: String? = null): HttpUrl {
        val url = "$apiBaseUrl/-WcGoRt8IQuPm-l1ymgy6g/Followers"

        val variables = JSONObject().also { json ->
            json.put("userId", userId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toHttpUrl().newBuilder().apply {
            addQueryParameter("variables", variables)
            addQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build()
    }

    val createTweet = "$apiBaseUrl/oB-5XsHNAbjvARJEc8CZFw/CreateTweet".toHttpUrl()
    val deleteTweet = "$apiBaseUrl/VaenaVgh5q5ih7kvyVjgtg/DeleteTweet".toHttpUrl()
    val createRetweet = "$apiBaseUrl/ojPdsZsimiJrUGLR1sjUtA/CreateRetweet".toHttpUrl()
    val deleteRetweet = "$apiBaseUrl/iQtK4dl5hBmXewYZuEOKVw/DeleteRetweet".toHttpUrl()
    val favoriteTweet = "$apiBaseUrl/lI07N6Otwv1PhnEgXILM7A/FavoriteTweet".toHttpUrl()
    val unfavoriteTweet = "$apiBaseUrl/ZYKSe-w7KEslx3JhSIk5LA/UnfavoriteTweet".toHttpUrl()

}
