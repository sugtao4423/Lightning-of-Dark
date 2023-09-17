package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable

data class HashtagEntity(private val json: JSONObject) : Serializable {

    val text: String = json.getString("text")

    private val indices = json.getJSONArray("indices")
    val start: Int = indices.getInt(0)
    val end: Int = indices.getInt(1)

}
