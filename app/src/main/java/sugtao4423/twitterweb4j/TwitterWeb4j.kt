package sugtao4423.twitterweb4j

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import sugtao4423.twitter4j.Relationship
import sugtao4423.twitter4j.Status
import sugtao4423.twitter4j.TwitterException
import sugtao4423.twitter4j.User
import sugtao4423.twitter4j.UserList
import sugtao4423.twitterweb4j.body.CreateRetweetBody
import sugtao4423.twitterweb4j.body.CreateTweetBody
import sugtao4423.twitterweb4j.body.DeleteRetweetBody
import sugtao4423.twitterweb4j.body.DeleteTweetBody
import sugtao4423.twitterweb4j.body.FavoriteTweetBody
import sugtao4423.twitterweb4j.body.UnfavoriteTweetBody
import sugtao4423.twitterweb4j.challenge.ClientTransaction
import sugtao4423.twitterweb4j.challenge.ClientTransactionUtils
import sugtao4423.twitterweb4j.media.MediaUpload
import sugtao4423.twitterweb4j.model.CreateTweet
import sugtao4423.twitterweb4j.model.CursorList
import sugtao4423.twitterweb4j.model.PagableCursorList
import sugtao4423.twitterweb4j.parser.JsonParserGraphQL
import sugtao4423.twitterweb4j.parser.JsonParserGraphQLTimeline
import sugtao4423.twitterweb4j.parser.JsonParserGraphQLUser
import sugtao4423.twitterweb4j.parser.JsonParserV1
import sugtao4423.twitterweb4j.url.UrlGraphQL
import sugtao4423.twitterweb4j.url.UrlV1

class TwitterWeb4j {

    companion object {
        const val DEFAULT_COUNT = 40
    }

    private val authenticatedHeaders: Headers

    @Throws(TwitterException::class)
    constructor(cookie: String) {
        val ct0 = cookie.split(";").find {
            it.trim().startsWith("ct0=")
        }?.substringAfter("ct0=")
        if (ct0.isNullOrEmpty()) {
            throw TwitterException("Invalid cookie: ct0 token not found.")
        }
        authenticatedHeaders = Connection.authenticatedHeaders(cookie, ct0)
    }

    private val client = OkHttpClient()
    private var clientTransaction: ClientTransaction? = null

    val media by lazy { MediaUpload(client, authenticatedHeaders) }

    @Throws(TwitterException::class)
    fun verifyCredentials(): User {
        val url = UrlV1.verifyCredentials
        val response = get(url)
        return JsonParserV1.parseUser(response)
    }

    @Throws(TwitterException::class)
    fun getUserLists(id: Long, reverse: Boolean = false): List<UserList> {
        val url = UrlV1.getUserLists(id, reverse)
        val response = get(url)
        return JsonParserV1.parseUserListsArray(response)
    }

    @Throws(TwitterException::class)
    fun getUserLists(screenName: String, reverse: Boolean = false): List<UserList> {
        val url = UrlV1.getUserLists(screenName, reverse)
        val response = get(url)
        return JsonParserV1.parseUserListsArray(response)
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
    fun homeLatestTimeline(count: Int = DEFAULT_COUNT, cursor: String? = null): CursorList<Status> {
        val url = UrlGraphQL.homeLatestTimeline(count, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseHomeLatestTimeline(response)
    }

    @Throws(TwitterException::class)
    fun mentionsTimeline(count: Int = DEFAULT_COUNT, cursor: String? = null): CursorList<Status> {
        val url = UrlGraphQL.mentionsTimeline(count, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseMentionsTimeline(response)
    }

    @Throws(TwitterException::class)
    fun listTweetsTimeline(
        listId: Long, count: Int = DEFAULT_COUNT, cursor: String? = null
    ): CursorList<Status> {
        val url = UrlGraphQL.listTweetsTimeline(listId, count, cursor)
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
        userId: Long, count: Int = DEFAULT_COUNT, cursor: String? = null
    ): CursorList<Status> {
        val url = UrlGraphQL.userTweetsAndReplies(userId, count, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseUserTweetsAndReplies(response, userId)
    }

    @Throws(TwitterException::class)
    fun favorites(
        userId: Long, count: Int = DEFAULT_COUNT, cursor: String? = null
    ): CursorList<Status> {
        val url = UrlGraphQL.likes(userId, count, cursor)
        val response = get(url)
        return JsonParserGraphQLTimeline.parseLikes(response)
    }

    @Throws(TwitterException::class)
    fun following(
        userId: Long, count: Int = DEFAULT_COUNT, cursor: String? = null
    ): PagableCursorList<User> {
        val url = UrlGraphQL.following(userId, count, cursor)
        val response = get(url)
        return JsonParserGraphQLUser.parseFollowing(response)
    }

    @Throws(TwitterException::class)
    fun followers(
        userId: Long, count: Int = DEFAULT_COUNT, cursor: String? = null
    ): PagableCursorList<User> {
        val url = UrlGraphQL.followers(userId, count, cursor)
        val response = get(url)
        return JsonParserGraphQLUser.parseFollowers(response)
    }

    @Throws(TwitterException::class)
    fun createTweet(tweet: CreateTweet): Status {
        val url = UrlGraphQL.createTweet
        val body = CreateTweetBody(url).get(tweet)
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
    fun createRetweet(tweetId: Long): Long {
        val url = UrlGraphQL.createRetweet
        val body = CreateRetweetBody(url).get(tweetId)
        val response = post(url, body)
        return JsonParserGraphQL.parseCreateRetweet(response)
    }

    @Throws(TwitterException::class)
    fun deleteRetweet(tweetId: Long): Long {
        val url = UrlGraphQL.deleteRetweet
        val body = DeleteRetweetBody(url).get(tweetId)
        val response = post(url, body)
        return JsonParserGraphQL.parseDeleteRetweet(response)
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
            val homePageHtml = execute("GET", ClientTransactionUtils.homePageUrl, headers = h)
            val ondemandFileUrl = ClientTransactionUtils.getOndemandFileUrl(homePageHtml)
            val ondemandFileContent = execute("GET", ondemandFileUrl, headers = h)

            clientTransaction = ClientTransaction(homePageHtml, ondemandFileContent)
        } catch (e: Exception) {
            e.printStackTrace()
            throw TwitterException("Failed to load client transaction data.", e)
        }
    }

    private fun buildRequestHeaders(method: String, urlPath: String): Headers =
        authenticatedHeaders.newBuilder().apply {
            clientTransaction?.let {
                val transactionId = it.generateTransactionId(method, urlPath)
                add("X-Client-Transaction-Id", transactionId)
            }
        }.build()

    @Throws(TwitterException::class)
    private fun get(url: HttpUrl): String = execute("GET", url)

    @Throws(TwitterException::class)
    private fun post(url: HttpUrl, body: RequestBody): String = execute("POST", url, body)

    @Throws(TwitterException::class)
    private fun execute(
        method: String,
        url: HttpUrl,
        body: RequestBody? = null,
        headers: Headers = buildRequestHeaders(method, url.encodedPath),
    ): String = client.send(method, url, body, headers)

}
