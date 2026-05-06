package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.model.EntityIndex
import twitter4j.URLEntity

data class QuotedStatusPermalinkJSONImpl(
    @Transient private val json: Json,
    @Transient private val overrideIndices: EntityIndex? = null,
) : URLEntity, EntityIndex(json, overrideIndices), java.io.Serializable {

    private val url = json["url"].string
    private val expandedURL = json["expanded"].string
    private val displayURL = json["display"].string

    override fun getText(): String = url
    override fun getURL(): String = url
    override fun getExpandedURL(): String = expandedURL
    override fun getDisplayURL(): String = displayURL

}
