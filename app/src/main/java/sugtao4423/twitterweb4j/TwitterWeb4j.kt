package sugtao4423.twitterweb4j

import sugtao4423.twitterweb4j.body.CreateRetweetBody
import sugtao4423.twitterweb4j.body.CreateTweetBody
import sugtao4423.twitterweb4j.body.DeleteRetweetBody
import sugtao4423.twitterweb4j.body.DeleteTweetBody
import sugtao4423.twitterweb4j.body.FavoriteTweetBody
import sugtao4423.twitterweb4j.body.UnfavoriteTweetBody
import sugtao4423.twitterweb4j.model.CursorList
import sugtao4423.twitterweb4j.parser.JsonParserGraphQL
import sugtao4423.twitterweb4j.parser.JsonParserGraphQLTimeline
import sugtao4423.twitterweb4j.parser.JsonParserV1
import sugtao4423.twitterweb4j.url.UrlGraphQL
import sugtao4423.twitterweb4j.url.UrlV1
import twitter4j.Paging
import twitter4j.Relationship
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.User
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

class TwitterWeb4j(private val csrfToken: String, private val cookie: String) {

    companion object {
        @JvmStatic
        val DEFAULT_PAGE_COUNT = 40
    }

    @Throws(TwitterException::class)
    fun getHomeTimeline(paging: Paging? = null): List<Status> {
        val url = UrlV1.homeTimeline(paging)
        val response = get(url, true)
        return JsonParserV1.parseStatusesArray(response)
    }

    @Throws(TwitterException::class)
    fun getMentionsTimeline(paging: Paging? = null): List<Status> {
        val url = UrlV1.mentionsTimeline(paging)
        val response = get(url, true)
        return JsonParserV1.parseStatusesArray(response)
    }

    @Throws(TwitterException::class)
    fun showUser(id: Long): User {
        val url = UrlV1.showUser(id)
        val response = get(url, true)
        return JsonParserV1.parseUser(response)
    }

    @Throws(TwitterException::class)
    fun showUser(screenName: String): User {
        val url = UrlV1.showUser(screenName)
        val response = get(url, true)
        return JsonParserV1.parseUser(response)
    }

    @Throws(TwitterException::class)
    fun showFriendship(sourceId: Long, targetId: Long): Relationship {
        val url = UrlV1.showFriendship(sourceId, targetId)
        val response = get(url)
        return JsonParserV1.parseRelationship(response)
    }

    @Throws(TwitterException::class)
    fun showFriendship(sourceScreenName: String, targetScreenName: String): Relationship {
        val url = UrlV1.showFriendship(sourceScreenName, targetScreenName)
        val response = get(url)
        return JsonParserV1.parseRelationship(response)
    }

    @Throws(TwitterException::class)
    fun listTweetsTimeline(
        listId: Long, count: Int? = null, cursor: String? = null
    ): CursorList<Status> {
        val url = UrlGraphQL.listTweetsTimeline(listId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url, true)
        return JsonParserGraphQLTimeline.parseListTweetsTimeline(response)
    }

    @Throws(TwitterException::class)
    fun userTweetsAndReplies(
        userId: Long, count: Int? = null, cursor: String? = null
    ): CursorList<Status> {
        val url = UrlGraphQL.userTweetsAndReplies(userId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url, true)
        return JsonParserGraphQLTimeline.parseUserTweetsAndReplies(response, userId)
    }

    @Throws(TwitterException::class)
    fun getFavorites(userId: Long, count: Int? = null, cursor: String? = null): CursorList<Status> {
        val url = UrlGraphQL.likes(userId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseLikes(response)
    }

    @Throws(TwitterException::class)
    fun createTweet(tweetText: String): Status {
        val url = UrlGraphQL.createTweet
        val body = CreateTweetBody(url).get(tweetText)
        val response = post(url, body)
        return JsonParserGraphQL.parseCreateTweet(response)
    }

    @Throws(TwitterException::class)
    fun deleteTweet(tweetId: Long) {
        val url = UrlGraphQL.deleteTweet
        val body = DeleteTweetBody(url).get(tweetId)
        val response = post(url, body)
        JsonParserGraphQL.parseDeleteTweet(response)
    }

    @Throws(TwitterException::class)
    fun createRetweet(tweetId: Long) {
        val url = UrlGraphQL.createRetweet
        val body = CreateRetweetBody(url).get(tweetId)
        val response = post(url, body)
        JsonParserGraphQL.parseCreateRetweet(response)
    }

    @Throws(TwitterException::class)
    fun deleteRetweet(tweetId: Long) {
        val url = UrlGraphQL.deleteRetweet
        val body = DeleteRetweetBody(url).get(tweetId)
        val response = post(url, body)
        JsonParserGraphQL.parseDeleteRetweet(response)
    }

    @Throws(TwitterException::class)
    fun favoriteTweet(tweetId: Long) {
        val url = UrlGraphQL.favoriteTweet
        val body = FavoriteTweetBody(url).get(tweetId)
        val response = post(url, body)
        JsonParserGraphQL.parseFavoriteTweet(response)
    }

    @Throws(TwitterException::class)
    fun unfavoriteTweet(tweetId: Long) {
        val url = UrlGraphQL.unfavoriteTweet
        val body = UnfavoriteTweetBody(url).get(tweetId)
        val response = post(url, body)
        JsonParserGraphQL.parseUnfavoriteTweet(response)
    }

    private fun setSession(conn: HttpURLConnection) {
        conn.setRequestProperty("Cookie", cookie)
        conn.setRequestProperty("X-Csrf-Token", csrfToken)
    }

    @Throws(TwitterException::class)
    private fun get(url: String, isTweetDeck: Boolean = false): String {
        return access("GET", url, null, null, isTweetDeck)
    }

    @Throws(TwitterException::class)
    private fun post(
        url: String,
        body: String,
        contentType: String = "application/json",
        isTweetDeck: Boolean = false
    ): String {
        return access("POST", url, body, contentType, isTweetDeck)
    }

    @Throws(TwitterException::class)
    private fun access(
        method: String,
        url: String,
        body: String? = null,
        contentType: String? = null,
        isTweetDeck: Boolean
    ): String {
        val u = URL(url)
        var conn: HttpsURLConnection? = null

        try {
            conn = u.openConnection() as HttpsURLConnection
            conn.apply {
                requestMethod = method
                Connection.setBaseHeaders(this, isTweetDeck)
                setSession(this)
                conn.setRequestProperty("Accept-Encoding", "gzip")
                if (method == "POST" && body != null) {
                    val bodyBytes = body.toByteArray()
                    setRequestProperty("Content-Type", contentType)
                    setRequestProperty("Content-Length", bodyBytes.size.toString())
                    doOutput = true
                    outputStream.use { it.write(bodyBytes) }
                }
            }

            val data = conn.let {
                if (it.contentEncoding == "gzip") GZIPInputStream(it.inputStream) else it.inputStream
            }.reader().use { it.readText() }
            return data
        } catch (e: IOException) {
            val statusCode = conn?.responseCode ?: -1
            throw TwitterException(e.message, e, statusCode)
        } finally {
            conn?.disconnect()
        }
    }

}
