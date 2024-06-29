package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.model.EntityIndex
import sugtao4423.twitterweb4j.nullString
import twitter4j.HashtagEntity
import twitter4j.JSONObject
import twitter4j.SymbolEntity

data class HashtagEntityJSONImpl(private val json: JSONObject) : HashtagEntity, SymbolEntity,
    EntityIndex(json), java.io.Serializable {

    private val text = json.nullString("text")

    override fun getText(): String? = text

}
