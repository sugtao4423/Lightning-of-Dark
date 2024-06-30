package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.model.EntityIndex
import sugtao4423.twitterweb4j.nullString
import twitter4j.JSONObject
import twitter4j.URLEntity

data class URLEntityJSONImpl(private val json: JSONObject) : URLEntity, EntityIndex(json),
    java.io.Serializable {

    private val url = json.getString("url")
    private val expandedURL = json.nullString("expanded_url")
    private val displayURL = json.nullString("display_url")

    override fun getText(): String = url
    override fun getURL(): String = url
    override fun getExpandedURL(): String? = expandedURL
    override fun getDisplayURL(): String? = displayURL

}
