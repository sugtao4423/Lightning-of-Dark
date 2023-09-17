package sugtao4423.twitterweb4j.parser

import org.json.JSONException
import org.json.JSONObject
import sugtao4423.twitterweb4j.model.Status

object JsonParserGraphQL {

    @Throws(JSONException::class)
    fun parseCreateTweet(response: String): Status {
        val json = JSONObject(response)
        val jsonPosition = listOf(
            "data", "create_tweet", "tweet_results", "result"
        )
        var statusJson = json
        jsonPosition.forEach { statusJson = statusJson.getJSONObject(it) }
        return Status(statusJson)
    }

}
