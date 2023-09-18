package sugtao4423.twitterweb4j.parser

import twitter4j.JSONArray
import twitter4j.JSONException
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.TwitterObjectFactory
import twitter4j.User

object JsonParserV1 {

    @Throws(TwitterException::class)
    fun parseStatusesArray(response: String): List<Status> {
        val json: JSONArray
        try {
            json = JSONArray(response)
        } catch (e: JSONException) {
            throw TwitterException(e)
        }

        val statuses = mutableListOf<Status>()
        for (i in 0 until json.length()) {
            val tweet = TwitterObjectFactory.createStatus(json.getJSONObject(i).toString())
            statuses.add(tweet)
        }
        return statuses.toList()
    }

    @Throws(TwitterException::class)
    fun parseUser(response: String): User {
        return TwitterObjectFactory.createUser(response)
    }

}
