package sugtao4423.twitterweb4j

import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders

object Connection {

    private const val twitterWebBearer =
        "AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA"

    val defaultHeaders: Headers = mapOf(
        "Accept-Language" to "en-US,en;q=0.9",
        "Cache-Control" to "no-cache",
        "Pragma" to "no-cache",
        "Referer" to "https://x.com/",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36",
        "X-Twitter-Active-User" to "yes",
        "X-Twitter-Client-Language" to "en",
    ).toHeaders()

    val authorizedHeaders: Headers = defaultHeaders.newBuilder().apply {
        add("Authorization", "Bearer $twitterWebBearer")
        add("X-Twitter-Auth-Type", "OAuth2Session")
    }.build()

}
