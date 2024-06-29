package sugtao4423.twitterweb4j

import twitter4j.JSONObject

/**
 * @return `null` if the key does not exist
 */
fun JSONObject.nullString(key: String): String? = optString(key, null)

/**
 * @return `false` if the key does not exist
 */
fun JSONObject.falseBoolean(key: String): Boolean = optBoolean(key, false)
