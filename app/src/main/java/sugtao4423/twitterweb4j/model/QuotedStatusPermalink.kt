package sugtao4423.twitterweb4j.model

import org.json.JSONObject
import java.io.Serializable

data class QuotedStatusPermalink(private val json: JSONObject) : Serializable {

    val url: String = json.getString("url")
    val expandedUrl: String = json.getString("expanded")
    val displayUrl: String = json.getString("display")

}
