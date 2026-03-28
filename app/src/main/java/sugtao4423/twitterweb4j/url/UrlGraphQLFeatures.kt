package sugtao4423.twitterweb4j.url

import org.json.JSONObject

object UrlGraphQLFeatures {

    @JvmStatic
    val listTweetsTimeline = JSONObject().apply {
        put("rweb_video_screen_enabled", false)
        put("payments_enabled", false)
        put("profile_label_improvements_pcf_label_in_post_enabled", true)
        put("rweb_tipjar_consumption_enabled", true)
        put("verified_phone_label_enabled", false)
        put("creator_subscriptions_tweet_preview_api_enabled", true)
        put("responsive_web_graphql_timeline_navigation_enabled", true)
        put("responsive_web_graphql_skip_user_profile_image_extensions_enabled", false)
        put("premium_content_api_read_enabled", false)
        put("communities_web_enable_tweet_community_results_fetch", true)
        put("c9s_tweet_anatomy_moderator_badge_enabled", true)
        put("responsive_web_grok_analyze_button_fetch_trends_enabled", false)
        put("responsive_web_grok_analyze_post_followups_enabled", true)
        put("responsive_web_jetfuel_frame", true)
        put("responsive_web_grok_share_attachment_enabled", true)
        put("articles_preview_enabled", true)
        put("responsive_web_edit_tweet_api_enabled", true)
        put("graphql_is_translatable_rweb_tweet_is_translatable_enabled", true)
        put("view_counts_everywhere_api_enabled", true)
        put("longform_notetweets_consumption_enabled", true)
        put("responsive_web_twitter_article_tweet_consumption_enabled", true)
        put("tweet_awards_web_tipping_enabled", false)
        put("responsive_web_grok_show_grok_translated_post", false)
        put("responsive_web_grok_analysis_button_from_backend", false)
        put("creator_subscriptions_quote_tweet_preview_enabled", false)
        put("freedom_of_speech_not_reach_fetch_enabled", true)
        put("standardized_nudges_misinfo", true)
        put("tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled", true)
        put("longform_notetweets_rich_text_read_enabled", true)
        put("longform_notetweets_inline_media_enabled", true)
        put("responsive_web_grok_image_annotation_enabled", true)
        put("responsive_web_grok_community_note_auto_translation_is_enabled", false)
        put("responsive_web_enhance_cards_enabled", false)
    }.toString()

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

    @JvmStatic
    val likes = JSONObject().apply {
        put("dont_mention_me_view_api_enabled", true)
        put("interactive_text_enabled", true)
        put("responsive_web_uc_gql_enabled", false)
        put("vibe_tweet_context_enabled", false)
        put("responsive_web_edit_tweet_api_enabled", false)
        put("standardized_nudges_misinfo", false)
        put("responsive_web_enhance_cards_enabled", false)
    }.toString()

}
