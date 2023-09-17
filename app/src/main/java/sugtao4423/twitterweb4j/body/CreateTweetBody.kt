package sugtao4423.twitterweb4j.body

class CreateTweetBody(requestUrl: String) : BaseBody(requestUrl) {

    override val features = mapOf(
        "tweetypie_unmention_optimization_enabled" to true,
        "responsive_web_edit_tweet_api_enabled" to true,
        "graphql_is_translatable_rweb_tweet_is_translatable_enabled" to true,
        "view_counts_everywhere_api_enabled" to true,
        "longform_notetweets_consumption_enabled" to true,
        "responsive_web_twitter_article_tweet_consumption_enabled" to false,
        "tweet_awards_web_tipping_enabled" to false,
        "longform_notetweets_rich_text_read_enabled" to true,
        "longform_notetweets_inline_media_enabled" to true,
        "responsive_web_graphql_exclude_directive_enabled" to true,
        "verified_phone_label_enabled" to false,
        "freedom_of_speech_not_reach_fetch_enabled" to true,
        "standardized_nudges_misinfo" to true,
        "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled" to true,
        "responsive_web_media_download_video_enabled" to false,
        "responsive_web_graphql_skip_user_profile_image_extensions_enabled" to false,
        "responsive_web_graphql_timeline_navigation_enabled" to true,
        "responsive_web_enhance_cards_enabled" to false,
    )

    override val variables = mapOf(
        "dark_request" to false,
        "semantic_annotation_ids" to emptyList<Any>(),
        "media" to mapOf(
            "media_entities" to emptyList<Any>(),
            "possibly_sensitive" to false,
        ),
    )

    fun get(tweetText: String): String {
        val variables = this.variables + mapOf("tweet_text" to tweetText)
        return buildJsonString(variables)
    }

}
