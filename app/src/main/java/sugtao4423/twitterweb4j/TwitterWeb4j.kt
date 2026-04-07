package sugtao4423.twitterweb4j

import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import sugtao4423.twitterweb4j.body.CreateRetweetBody
import sugtao4423.twitterweb4j.body.CreateTweetBody
import sugtao4423.twitterweb4j.body.DeleteRetweetBody
import sugtao4423.twitterweb4j.body.DeleteTweetBody
import sugtao4423.twitterweb4j.body.FavoriteTweetBody
import sugtao4423.twitterweb4j.body.UnfavoriteTweetBody
import sugtao4423.twitterweb4j.challenge.ClientTransaction
import sugtao4423.twitterweb4j.challenge.ClientTransactionUtils
import sugtao4423.twitterweb4j.model.CursorList
import sugtao4423.twitterweb4j.model.PagableCursorList
import sugtao4423.twitterweb4j.parser.JsonParserGraphQL
import sugtao4423.twitterweb4j.parser.JsonParserGraphQLTimeline
import sugtao4423.twitterweb4j.parser.JsonParserGraphQLUser
import sugtao4423.twitterweb4j.parser.JsonParserV1
import sugtao4423.twitterweb4j.url.UrlGraphQL
import sugtao4423.twitterweb4j.url.UrlV1
import twitter4j.Relationship
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.User
import java.io.IOException
import java.net.URL

class TwitterWeb4j(private val csrfToken: String, private val cookie: String) {

    companion object {
        @JvmStatic
        val DEFAULT_PAGE_COUNT = 40

        @JvmStatic
        val CONTENT_TYPE_JSON = "application/json".toMediaType()
    }

    private val client = OkHttpClient()
    private var clientTransaction: ClientTransaction? = null

    @Throws(TwitterException::class)
    fun verifyCredentials(): User {
        val url = UrlV1.verifyCredentials
        val response = get(url)
        return JsonParserV1.parseUser(response)
    }

    @Throws(TwitterException::class)
    fun showUser(id: Long): User {
        val url = UrlV1.showUser(id)
        val response = get(url)
        return JsonParserV1.parseUser(response)
    }

    @Throws(TwitterException::class)
    fun showUser(screenName: String): User {
        val url = UrlV1.showUser(screenName)
        val response = get(url)
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
    fun homeLatestTimeline(count: Int? = null, cursor: String? = null): CursorList<Status> {
        val url = UrlGraphQL.homeLatestTimeline(count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseHomeLatestTimeline(response)
    }

    @Throws(TwitterException::class)
    fun mentionsTimeline(count: Int? = null, cursor: String? = null): CursorList<Status> {
        val url = UrlGraphQL.mentionsTimeline(count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseMentionsTimeline(response)
    }

    @Throws(TwitterException::class)
    fun listTweetsTimeline(
        listId: Long, count: Int? = null, cursor: String? = null
    ): CursorList<Status> {
        val url = UrlGraphQL.listTweetsTimeline(listId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseListTweetsTimeline(response)
    }

    @Throws(TwitterException::class)
    fun tweetDetail(tweetId: Long): Status {
        val url = UrlGraphQL.tweetDetail(tweetId)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseTweetDetail(response, tweetId)
    }

    @Throws(TwitterException::class)
    fun userTweetsAndReplies(
        userId: Long, count: Int? = null, cursor: String? = null
    ): CursorList<Status> {
        val url = UrlGraphQL.userTweetsAndReplies(userId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseUserTweetsAndReplies(response, userId)
    }

    @Throws(TwitterException::class)
    fun favorites(userId: Long, count: Int? = null, cursor: String? = null): CursorList<Status> {
        val url = UrlGraphQL.likes(userId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseLikes(response)
    }

    @Throws(TwitterException::class)
    fun following(
        userId: Long, count: Int? = null, cursor: String? = null
    ): PagableCursorList<User> {
        val url = UrlGraphQL.following(userId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLUser.parseFollowing(response)
    }

    @Throws(TwitterException::class)
    fun followers(
        userId: Long, count: Int? = null, cursor: String? = null
    ): PagableCursorList<User> {
        val url = UrlGraphQL.followers(userId, count ?: DEFAULT_PAGE_COUNT, cursor)
        val response = get(url)
        return JsonParserGraphQLUser.parseFollowers(response)
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

    @Throws(TwitterException::class)
    fun loadClientTransaction() {
        val h = Connection.defaultHeaders
        try {
            val homePageHtml = access("GET", ClientTransactionUtils.homePageUrl, headers = h)
            val ondemandFileUrl = ClientTransactionUtils.getOndemandFileUrl(homePageHtml)
            val ondemandFileContent = access("GET", ondemandFileUrl, headers = h)

            clientTransaction = ClientTransaction(homePageHtml, ondemandFileContent)
        } catch (e: Exception) {
            e.printStackTrace()
            throw TwitterException("Failed to load client transaction data.", e)
        }
    }

    private fun buildRequestHeaders(method: String, urlPath: String): Headers =
        Connection.authorizedHeaders.newBuilder().apply {
            add("Cookie", cookie)
            add("X-Csrf-Token", csrfToken)

            clientTransaction?.let {
                val transactionId = it.generateTransactionId(method, urlPath)
                add("X-Client-Transaction-Id", transactionId)
            }
        }.build()

    @Throws(TwitterException::class)
    private fun get(url: String): String = access("GET", url)

    @Throws(TwitterException::class)
    private fun post(
        url: String, body: String, contentType: MediaType = CONTENT_TYPE_JSON
    ): String = access("POST", url, body, contentType)

    @Throws(TwitterException::class)
    private fun access(
        method: String,
        url: String,
        body: String? = null,
        contentType: MediaType? = null,
        headers: Headers = buildRequestHeaders(method, URL(url).path)
    ): String {
        val requestBody = if (method == "POST" && body != null) {
            body.toRequestBody(contentType ?: CONTENT_TYPE_JSON)
        } else {
            null
        }

        val request = Request.Builder().apply {
            url(url)
            method(method, requestBody)
            headers(headers)
        }.build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            return response.body.string()
        } catch (e: IOException) {
            e.printStackTrace()
            throw TwitterException(e)
        }
    }

}
