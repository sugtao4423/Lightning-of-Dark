package sugtao4423.twitterweb4j.url

import twitter4j.JSONObject

object UrlGraphQLFeatures {

    @JvmStatic
    private val defaultFeatures: Map<String, Boolean> = mapOf(
        "responsive_web_graphql_exclude_directive_enabled" to true,
        "verified_phone_label_enabled" to true,
        "responsive_web_graphql_skip_user_profile_image_extensions_enabled" to false,
        "responsive_web_graphql_timeline_navigation_enabled" to true,
    )

    @JvmStatic
    private val userDataFeatures: Map<String, Boolean> = mapOf(
        "hidden_profile_likes_enabled" to false,
        "hidden_profile_subscriptions_enabled" to true,
        "highlights_tweets_tab_ui_enabled" to true,
        "responsive_web_twitter_article_notes_tab_enabled" to false,
        "creator_subscriptions_tweet_preview_api_enabled" to true,
    )

    @JvmStatic
    private val userInfoFeatures: Map<String, Boolean> = mapOf(
        "subscriptions_verification_info_is_identity_verified_enabled" to true,
        "subscriptions_verification_info_verified_since_enabled" to true,
    )

    @JvmStatic
    private val additionalFeatures: Map<String, Boolean> = mapOf(
        "rweb_lists_timeline_redesign_enabled" to true,
        "creator_subscriptions_tweet_preview_api_enabled" to true,
        "c9s_tweet_anatomy_moderator_badge_enabled" to true,
        "tweetypie_unmention_optimization_enabled" to true,
        "responsive_web_edit_tweet_api_enabled" to true,
        "graphql_is_translatable_rweb_tweet_is_translatable_enabled" to true,
        "view_counts_everywhere_api_enabled" to true,
        "longform_notetweets_consumption_enabled" to true,
        "responsive_web_twitter_article_tweet_consumption_enabled" to false,
        "tweet_awards_web_tipping_enabled" to false,
        "freedom_of_speech_not_reach_fetch_enabled" to true,
        "standardized_nudges_misinfo" to true,
        "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled" to false,
        "rweb_video_timestamps_enabled" to true,
        "longform_notetweets_rich_text_read_enabled" to true,
        "longform_notetweets_inline_media_enabled" to false,
        "responsive_web_media_download_video_enabled" to false,
        "responsive_web_enhance_cards_enabled" to false,
    )

    @JvmStatic
    fun generateMap(
        default: Boolean = true,
        userData: Boolean = false,
        userInfo: Boolean = false,
        additional: Boolean = false,
    ): Map<String, Boolean> {
        val features = mutableMapOf<String, Boolean>()
        if (default) {
            features += defaultFeatures
        }
        if (userData || userInfo) {
            features += userDataFeatures
        }
        if (userInfo) {
            features += userInfoFeatures
        }
        if (additional) {
            features += additionalFeatures
        }
        return features.toMap()
    }

    @JvmStatic
    fun generate(
        default: Boolean = true,
        userData: Boolean = false,
        userInfo: Boolean = false,
        additional: Boolean = false,
    ): String = JSONObject(generateMap(default, userData, userInfo, additional)).toString()

}
