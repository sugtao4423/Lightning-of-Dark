package sugtao4423.twitterweb4j

import sugtao4423.twitterweb4j.body.CreateTweetBody
import sugtao4423.twitterweb4j.parser.JsonParserGraphQL
import sugtao4423.twitterweb4j.parser.JsonParserV1
import sugtao4423.twitterweb4j.url.UrlGraphQL
import sugtao4423.twitterweb4j.url.UrlV1
import twitter4j.JSONException
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import sugtao4423.twitterweb4j.model.Status as StatusV2

class TwitterWeb4j(private val csrfToken: String, private val cookie: String) {

    @Throws(IOException::class, JSONException::class, TwitterException::class)
    fun getHomeTimeline(paging: Paging? = null): List<Status> {
        val url = UrlV1.homeTimeline(paging)
        val response = get(url)
        return JsonParserV1.parseStatusesArray(response)
    }

    @Throws(IOException::class, JSONException::class, TwitterException::class)
    fun getMentionsTimeline(paging: Paging? = null): List<Status> {
        val url = UrlV1.mentionsTimeline(paging)
        val response = get(url)
        return JsonParserV1.parseStatusesArray(response)
    }

    @Throws(IOException::class, JSONException::class, TwitterException::class)
    fun getUserListStatuses(listId: Long, paging: Paging? = null): List<Status> {
        val url = UrlV1.userListStatuses(listId, paging)
        val response = get(url)
        return JsonParserV1.parseStatusesArray(response)
    }

    @Throws(IOException::class, JSONException::class)
    fun createTweet(tweetText: String): StatusV2 {
        val url = UrlGraphQL.createTweet
        val body = CreateTweetBody.get(tweetText)
        val response = post(url, body)
        return JsonParserGraphQL.parseCreateTweet(response)
    }

    private fun setSession(conn: HttpURLConnection) {
        conn.setRequestProperty("Cookie", cookie)
        conn.setRequestProperty("X-Csrf-Token", csrfToken)
    }

    @Throws(IOException::class)
    private fun get(url: String): String {
        return access("GET", url)
    }

    @Throws(IOException::class)
    private fun post(url: String, body: String, contentType: String = "application/json"): String {
        return access("POST", url, body, contentType)
    }

    @Throws(IOException::class)
    private fun access(
        method: String, url: String, body: String? = null, contentType: String? = null
    ): String {
        val u = URL(url)
        val conn = u.openConnection() as HttpsURLConnection

        conn.apply {
            requestMethod = method
            Connection.setBaseHeaders(this)
            setSession(this)
            if (method == "POST" && body != null) {
                setRequestProperty("Content-Type", contentType)
                setRequestProperty("Content-Length", body.length.toString())
                doOutput = true
                outputStream.use { it.write(body.toByteArray()) }
            }
        }

        val data = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return data
    }

}
