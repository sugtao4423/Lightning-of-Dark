package sugtao4423.twitterweb4j.impl

import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.model.EntityIndex
import twitter4j.HashtagEntity
import twitter4j.SymbolEntity

data class HashtagEntityJSONImpl(@Transient private val json: Json) : HashtagEntity, SymbolEntity,
    EntityIndex(json), java.io.Serializable {

    private val text = json["text"].stringOrNull

    override fun getText(): String? = text

}
