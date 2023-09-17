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

    }
}
