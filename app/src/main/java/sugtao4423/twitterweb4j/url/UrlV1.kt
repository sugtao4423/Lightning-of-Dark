package sugtao4423.twitterweb4j.url

import twitter4j.Paging

class UrlV1 : UrlBase() {
    companion object {

        @JvmStatic
        private val homeTimeline = "$v1ApiBaseUrl/statuses/home_timeline.json"

        @JvmStatic
        private val mentionsTimeline = "$v1ApiBaseUrl/statuses/mentions_timeline.json"

        @JvmStatic
        private val userListStatuses = "$v1ApiBaseUrl/lists/statuses.json"

        @JvmStatic
        private val showUser = "$v1ApiBaseUrl/users/show.json"

        @JvmStatic
        private val showFriendship = "$v1ApiBaseUrl/friendships/show.json"

        @JvmStatic
        fun homeTimeline(paging: Paging? = null): String {
            val params = buildPaginatedQueryParams(v1ApiBaseQueryParams, paging)
            return "$homeTimeline?$params"
        }

        @JvmStatic
        fun mentionsTimeline(paging: Paging? = null): String {
            val params = buildPaginatedQueryParams(v1ApiBaseQueryParams, paging)
            return "$mentionsTimeline?$params"
        }

        @JvmStatic
        fun userListStatuses(listId: Long, paging: Paging? = null): String {
            val query = v1ApiBaseQueryParams + mapOf(
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

    }
}
