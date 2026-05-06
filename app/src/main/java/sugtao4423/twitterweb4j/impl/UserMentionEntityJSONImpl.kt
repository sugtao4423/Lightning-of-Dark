package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.model.EntityIndex
import twitter4j.UserMentionEntity

data class UserMentionEntityJSONImpl(
    @Transient private val json: Json,
    @Transient private val overrideIndices: EntityIndex? = null,
) : UserMentionEntity, EntityIndex(json, overrideIndices), java.io.Serializable {

    private val id = json["id_str"].string.toLong()
    private val name = json["name"].string
    private val screenName = json["screen_name"].string

    override fun getText(): String = screenName
    override fun getName(): String = name
    override fun getScreenName(): String = screenName
    override fun getId(): Long = id

}
