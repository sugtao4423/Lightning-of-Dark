package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.model.EntityIndex
import twitter4j.JSONObject
import twitter4j.UserMentionEntity

data class UserMentionEntityJSONImpl(private val json: JSONObject) : UserMentionEntity,
    EntityIndex(json), java.io.Serializable {

    private val id = json.getString("id_str").toLong()
    private val name = json.getString("name")
    private val screenName = json.getString("screen_name")

    override fun getText(): String = screenName
    override fun getName(): String = name
    override fun getScreenName(): String = screenName
    override fun getId(): Long = id

}
