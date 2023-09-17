package sugtao4423.twitterweb4j.url

import twitter4j.Paging
import java.net.URLEncoder

abstract class UrlBase {
    companion object {

        @JvmStatic
        protected val v1ApiBaseUrl = "https://api.twitter.com/1.1"

        @JvmStatic
        protected val graphQLApiBaseUrl = "https://twitter.com/i/api/graphql"

        @JvmStatic
        protected val DEFAULT_PAGE_COUNT = 40

        @JvmStatic
        protected val v1ApiBaseQueryParams = mapOf(
            "count" to DEFAULT_PAGE_COUNT.toString(),
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
        protected fun buildQueryParams(params: Map<String, String>): String {
            val sb = StringBuilder()
            params.forEach { (key, value) ->
                val encodedValue = URLEncoder.encode(value, "UTF-8")
                sb.append("&$key=$encodedValue")
            }
            return sb.toString().substring(1)
        }

        @JvmStatic
        protected fun buildPaginatedQueryParams(
            params: Map<String, String>, paging: Paging?
        ): String {
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
}
