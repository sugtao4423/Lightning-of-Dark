package sugtao4423.twitterweb4j.url

import java.net.URLEncoder

object UrlV1 {

    private val apiBaseUrl = "https://api.twitter.com/1.1".replace("twitter", "x")

    val verifyCredentials = "$apiBaseUrl/account/verify_credentials.json"

    private val getUserLists = "$apiBaseUrl/lists/list.json"
    private val showUser = "$apiBaseUrl/users/show.json"
    private val showFriendship = "$apiBaseUrl/friendships/show.json"

    fun getUserLists(id: Long, reverse: Boolean): String {
        val query = mapOf("user_id" to id.toString(), "reverse" to reverse.toString())
        val params = buildQueryParams(query)
        return "$getUserLists?$params"
    }

    fun getUserLists(screenName: String, reverse: Boolean): String {
        val query = mapOf("screen_name" to screenName, "reverse" to reverse.toString())
        val params = buildQueryParams(query)
        return "$getUserLists?$params"
    }

    fun showUser(id: Long): String {
        val query = mapOf("user_id" to id.toString(), "include_entities" to "true")
        val params = buildQueryParams(query)
        return "$showUser?$params"
    }

    fun showUser(screenName: String): String {
        val query = mapOf("screen_name" to screenName, "include_entities" to "true")
        val params = buildQueryParams(query)
        return "$showUser?$params"
    }

    fun showFriendship(sourceId: Long, targetId: Long): String {
        val query = mapOf(
            "source_id" to sourceId.toString(),
            "target_id" to targetId.toString(),
        )
        val params = buildQueryParams(query)
        return "$showFriendship?$params"
    }

    fun showFriendship(sourceScreenName: String, targetScreenName: String): String {
        val query = mapOf(
            "source_screen_name" to sourceScreenName,
            "target_screen_name" to targetScreenName,
        )
        val params = buildQueryParams(query)
        return "$showFriendship?$params"
    }

    private fun buildQueryParams(params: Map<String, String>): String {
        val sb = StringBuilder()
        params.forEach { (key, value) ->
            val encodedValue = URLEncoder.encode(value, "UTF-8")
            sb.append("&$key=$encodedValue")
        }
        return sb.toString().substring(1)
    }

}
