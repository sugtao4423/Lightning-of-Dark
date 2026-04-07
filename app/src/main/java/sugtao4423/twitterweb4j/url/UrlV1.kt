package sugtao4423.twitterweb4j.url

import java.net.URLEncoder

object UrlV1 {

    @JvmStatic
    private val apiBaseUrl = "https://api.twitter.com/1.1".replace("twitter", "x")

    @JvmStatic
    private val showUser = "$apiBaseUrl/users/show.json"

    @JvmStatic
    private val showFriendship = "$apiBaseUrl/friendships/show.json"

    @JvmStatic
    fun showUser(id: Long): String {
        val query = mapOf("user_id" to id.toString(), "include_entities" to "true")
        val params = buildQueryParams(query)
        return "$showUser?$params"
    }

    @JvmStatic
    fun showUser(screenName: String): String {
        val query = mapOf("screen_name" to screenName, "include_entities" to "true")
        val params = buildQueryParams(query)
        return "$showUser?$params"
    }

    @JvmStatic
    fun showFriendship(sourceId: Long, targetId: Long): String {
        val query = mapOf(
            "source_id" to sourceId.toString(),
            "target_id" to targetId.toString(),
        )
        val params = buildQueryParams(query)
        return "$showFriendship?$params"
    }

    @JvmStatic
    fun showFriendship(sourceScreenName: String, targetScreenName: String): String {
        val query = mapOf(
            "source_screen_name" to sourceScreenName,
            "target_screen_name" to targetScreenName,
        )
        val params = buildQueryParams(query)
        return "$showFriendship?$params"
    }

    @JvmStatic
    private fun buildQueryParams(params: Map<String, String>): String {
        val sb = StringBuilder()
        params.forEach { (key, value) ->
            val encodedValue = URLEncoder.encode(value, "UTF-8")
            sb.append("&$key=$encodedValue")
        }
        return sb.toString().substring(1)
    }

}
