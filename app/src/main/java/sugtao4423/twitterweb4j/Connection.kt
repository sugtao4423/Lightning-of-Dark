package sugtao4423.twitterweb4j

import java.net.HttpURLConnection

object Connection {

    private const val authorizationBearer =
        "AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA"

    private const val userAgent =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36"

    private const val twitterActiveUser =
        "yes"

    private const val twitterAuthType =
        "OAuth2Session"

    fun setBaseHeaders(conn: HttpURLConnection) {
        conn.setRequestProperty("Authorization", "Bearer $authorizationBearer")
        conn.setRequestProperty("User-Agent", userAgent)
        conn.setRequestProperty("X-Twitter-Active-User", twitterActiveUser)
        conn.setRequestProperty("X-Twitter-Auth-Type", twitterAuthType)
    }

}
