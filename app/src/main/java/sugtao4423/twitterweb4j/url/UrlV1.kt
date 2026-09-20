package sugtao4423.twitterweb4j.url

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object UrlV1 {

    private val apiBaseUrl = "https://api.twitter.com/1.1".replace("twitter", "x")

    val verifyCredentials = "$apiBaseUrl/account/verify_credentials.json".toHttpUrl()

    private val getUserLists = "$apiBaseUrl/lists/list.json".toHttpUrl()
    private val showUser = "$apiBaseUrl/users/show.json".toHttpUrl()
    private val showFriendship = "$apiBaseUrl/friendships/show.json".toHttpUrl()

    fun getUserLists(id: Long, reverse: Boolean): HttpUrl = getUserLists.newBuilder().apply {
        addQueryParameter("user_id", id.toString())
        addQueryParameter("reverse", reverse.toString())
    }.build()

    fun getUserLists(screenName: String, reverse: Boolean): HttpUrl =
        getUserLists.newBuilder().apply {
            addQueryParameter("screen_name", screenName)
            addQueryParameter("reverse", reverse.toString())
        }.build()

    fun showUser(id: Long): HttpUrl = showUser.newBuilder().apply {
        addQueryParameter("user_id", id.toString())
        addQueryParameter("include_entities", "true")
    }.build()

    fun showUser(screenName: String): HttpUrl = showUser.newBuilder().apply {
        addQueryParameter("screen_name", screenName)
        addQueryParameter("include_entities", "true")
    }.build()

    fun showFriendship(sourceId: Long, targetId: Long): HttpUrl =
        showFriendship.newBuilder().apply {
            addQueryParameter("source_id", sourceId.toString())
            addQueryParameter("target_id", targetId.toString())
        }.build()

    fun showFriendship(sourceScreenName: String, targetScreenName: String): HttpUrl =
        showFriendship.newBuilder().apply {
            addQueryParameter("source_screen_name", sourceScreenName)
            addQueryParameter("target_screen_name", targetScreenName)
        }.build()

}
