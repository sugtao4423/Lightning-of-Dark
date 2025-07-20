package sugtao4423.twitterweb4j.url

import android.net.Uri
import org.json.JSONObject

object UrlGraphQL {

    @JvmStatic
    private val apiBaseUrl = "https://twitter.com/i/api/graphql".replace("twitter", "x")

    @JvmStatic
    fun listTweetsTimeline(listId: Long, count: Int, cursor: String? = null): String {
        val url = "$apiBaseUrl/l411pL-GRg-AKo_a2rmYjg/ListLatestTweetsTimeline"

        val variables = JSONObject().also { json ->
            json.put("listId", listId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return Uri.parse(url).buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.listTweetsTimeline)
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

        return Uri.parse(url).buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.userTweetsAndReplies)
        }.build().toString()
    }

    @JvmStatic
    fun likes(userId: Long, count: Int, cursor: String? = null): String {
        val url = "$apiBaseUrl/ayhH-V7xvuv4nPZpkpuhFA/Likes"

        val variables = JSONObject().also { json ->
            json.put("userId", userId.toString())
            json.put("count", count)
            json.put("includePromotedContent", false)
            json.put("withClientEventToken", false)
            json.put("withBirdwatchNotes", false)
            json.put("withVoice", true)
            json.put("withV2Timeline", true)
            cursor?.let { json.put("cursor", it) }
        }.toString()

        return Uri.parse(url).buildUpon().apply {
            appendQueryParameter("variables", variables)
            appendQueryParameter("features", UrlGraphQLFeatures.likes)
        }.build().toString()
    }

    @JvmStatic
    val createTweet = "$apiBaseUrl/xT36w0XM3A8jDynpkram2A/CreateTweet"

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
