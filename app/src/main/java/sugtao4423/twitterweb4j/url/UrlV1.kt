package sugtao4423.twitterweb4j.url

import sugtao4423.twitterweb4j.TwitterWeb4j
import twitter4j.Paging
import java.net.URLEncoder

object UrlV1 {

    @JvmStatic
    private val apiBaseUrl = "https://api.twitter.com/1.1"

    @JvmStatic
    private val baseQueryParams = mapOf(
        "count" to TwitterWeb4j.DEFAULT_PAGE_COUNT.toString(),
        "include_my_retweet" to "1",
        "cards_platform" to "Web-13",
        "include_entities" to "1",
        "include_user_entities" to "1",
        "include_cards" to "1",
        "send_error_codes" to "1",
        "tweet_mode" to "extended",
        "include_ext_alt_text" to "true",
        "include_reply_count" to "true",
        "ext" to "mediaStats,highlightedLabel,voiceInfo,superFollowMetadata",
        "include_ext_has_nft_avatar" to "true",
        "include_ext_is_blue_verified" to "true",
        "include_ext_verified_type" to "true",
        "include_ext_sensitive_media_warning" to "true",
        "include_ext_media_color" to "true",
    )

    @JvmStatic
    private val homeTimeline = "$apiBaseUrl/statuses/home_timeline.json"

    @JvmStatic
    private val mentionsTimeline = "$apiBaseUrl/statuses/mentions_timeline.json"

    @JvmStatic
    private val userListStatuses = "$apiBaseUrl/lists/statuses.json"

    @JvmStatic
    private val showUser = "$apiBaseUrl/users/show.json"

    @JvmStatic
    private val showFriendship = "$apiBaseUrl/friendships/show.json"

    @JvmStatic
    fun homeTimeline(paging: Paging? = null): String {
        val params = buildPaginatedQueryParams(baseQueryParams, paging)
        return "$homeTimeline?$params"
    }

    @JvmStatic
    fun mentionsTimeline(paging: Paging? = null): String {
        val params = buildPaginatedQueryParams(baseQueryParams, paging)
        return "$mentionsTimeline?$params"
    }

    @JvmStatic
    fun userListStatuses(listId: Long, paging: Paging? = null): String {
        val query = baseQueryParams + mapOf(
            "list_id" to listId.toString(), "include_rts" to "1"
        )
        val params = buildPaginatedQueryParams(query, paging)
        return "$userListStatuses?$params"
    }

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

    @JvmStatic
    private fun buildPaginatedQueryParams(params: Map<String, String>, paging: Paging?): String {
        if (paging == null) {
            return buildQueryParams(params)
        }

        val newParams = params.toMutableMap()
        if (paging.count > 0) {
            newParams["count"] = paging.count.toString()
        }
        if (paging.sinceId > 0) {
            newParams["since_id"] = paging.sinceId.toString()
        }
        if (paging.maxId > 0) {
            newParams["max_id"] = paging.maxId.toString()
        }
        return buildQueryParams(newParams)
    }

}
