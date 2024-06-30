package sugtao4423.twitterweb4j.url

import org.json.JSONObject

object UrlGraphQLFeatures {

    @JvmStatic
    val userTweetsAndReplies = JSONObject().apply {
        put("rweb_lists_timeline_redesign_enabled", false)
        put("responsive_web_graphql_exclude_directive_enabled", true)
        put("verified_phone_label_enabled", false)
        put("creator_subscriptions_tweet_preview_api_enabled", true)
        put("responsive_web_graphql_timeline_navigation_enabled", true)
        put("responsive_web_graphql_skip_user_profile_image_extensions_enabled", false)
        put("tweetypie_unmention_optimization_enabled", true)
        put("responsive_web_edit_tweet_api_enabled", true)
        put("graphql_is_translatable_rweb_tweet_is_translatable_enabled", true)
        put("view_counts_everywhere_api_enabled", true)
        put("longform_notetweets_consumption_enabled", true)
        put("responsive_web_twitter_article_tweet_consumption_enabled", false)
        put("tweet_awards_web_tipping_enabled", false)
        put("freedom_of_speech_not_reach_fetch_enabled", true)
        put("standardized_nudges_misinfo", true)
        put("tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled", true)
        put("longform_notetweets_rich_text_read_enabled", true)
        put("longform_notetweets_inline_media_enabled", true)
        put("responsive_web_media_download_video_enabled", false)
        put("responsive_web_enhance_cards_enabled", false)
    }.toString()

}
