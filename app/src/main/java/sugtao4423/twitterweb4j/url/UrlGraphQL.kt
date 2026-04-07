package sugtao4423.twitterweb4j.url

import androidx.core.net.toUri
import twitter4j.JSONObject

object UrlGraphQL {

    @JvmStatic
    private val apiBaseUrl = "https://twitter.com/i/api/graphql".replace("twitter", "x")

    @JvmStatic
    fun homeLatestTimeline(count: Int, cursor: String? = null): String {
        val url = "$apiBaseUrl/cWF3cqWadLlIXA6KJWhcew/HomeLatestTimeline"

        val variables = JSONObject().also { json ->
            json.put("count", count)
            json.put("includePromotedContent", false)
            json.put("latestControlAvailable", true)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toUri().buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build().toString()
    }

    @JvmStatic
    fun mentionsTimeline(count: Int, cursor: String? = null): String {
        val url = "$apiBaseUrl/8bj3MP0KXWKlpfC1yvGfbQ/NotificationsTimeline"

        val variables = JSONObject().also { json ->
            json.put("timeline_type", "Mentions")
            json.put("count", count)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toUri().buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build().toString()
    }

    @JvmStatic
    fun listTweetsTimeline(listId: Long, count: Int, cursor: String? = null): String {
        val url = "$apiBaseUrl/l411pL-GRg-AKo_a2rmYjg/ListLatestTweetsTimeline"

        val variables = JSONObject().also { json ->
            json.put("listId", listId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return url.toUri().buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build().toString()
    }

    @JvmStatic
    fun userTweetsAndReplies(userId: Long, count: Int, cursor: String? = null): String {
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

        return url.toUri().buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build().toString()
    }

    @JvmStatic
    fun likes(userId: Long, count: Int, cursor: String? = null): String {
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

        return url.toUri().buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.generate(additional = true))
        }.build().toString()
    }

    @JvmStatic
    val createTweet = "$apiBaseUrl/oB-5XsHNAbjvARJEc8CZFw/CreateTweet"

    @JvmStatic
    val deleteTweet = "$apiBaseUrl/VaenaVgh5q5ih7kvyVjgtg/DeleteTweet"

    @JvmStatic
    val createRetweet = "$apiBaseUrl/ojPdsZsimiJrUGLR1sjUtA/CreateRetweet"

    @JvmStatic
    val deleteRetweet = "$apiBaseUrl/iQtK4dl5hBmXewYZuEOKVw/DeleteRetweet"

    @JvmStatic
    val favoriteTweet = "$apiBaseUrl/lI07N6Otwv1PhnEgXILM7A/FavoriteTweet"

    @JvmStatic
    val unfavoriteTweet = "$apiBaseUrl/ZYKSe-w7KEslx3JhSIk5LA/UnfavoriteTweet"

}
