package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable

data class URLEntity(private val json: JSONObject) : Serializable {

    val url: String = json.getString("url")
    val expandedUrl: String = json.getString("expanded_url")
    val displayUrl: String = json.getString("display_url")

    private val indices = json.getJSONArray("indices")
    val start: Int = indices.getInt(0)
    val end: Int = indices.getInt(1)

}
