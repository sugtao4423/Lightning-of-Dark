package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable

data class UserMentionEntity(private val json: JSONObject) : Serializable {

    val id: Long = json.getString("id_str").toLong()
    val name: String = json.getString("name")
    val screenName: String = json.getString("screen_name")

    private val indices = json.getJSONArray("indices")
    val start: Int = indices.getInt(0)
    val end: Int = indices.getInt(1)

}
