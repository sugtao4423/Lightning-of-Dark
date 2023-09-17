package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable

data class SymbolEntity(private val jsonObject: JSONObject) : Serializable {

    private val indices = jsonObject.getJSONArray("indices")
    val start: Int = indices.getInt(0)
    val end: Int = indices.getInt(1)

}
