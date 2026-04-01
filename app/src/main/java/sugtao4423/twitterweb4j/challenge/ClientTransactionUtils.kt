package sugtao4423.twitterweb4j.challenge

import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object ClientTransactionUtils {

    private val ON_DEMAND_FILE_REGEX = Regex(""",(\d+):["']ondemand\.s["']""")
    private val ON_DEMAND_HASH_PATTERN = """,%s:"([0-9a-f]+)""""

    val homePageUrl = "https://x.com"

    fun getOndemandFileUrl(homePageHtml: String): String {
        val fileMatch = ON_DEMAND_FILE_REGEX.find(homePageHtml)
            ?: throw IllegalStateException("Could not find \"ondemand.s\" reference in homepage HTML. The page structure may have changed.")
        val fileNum = fileMatch.groupValues[1]

        val fileHashMatch = ON_DEMAND_HASH_PATTERN.format(fileNum).toRegex().find(homePageHtml)
            ?: throw IllegalStateException("Could not find hash for \"ondemand.s\" file number $fileNum in homepage HTML. The page structure may have changed.")
        val hash = fileHashMatch.groupValues[1]
        return "https://abs.twimg.com/responsive-web/client-web/ondemand.s.${hash}a.js"
    }

    private val defaultHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36",
        "Accept-Language" to "en-US,en;q=0.9",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Cache-Control" to "no-cache",
        "Pragma" to "no-cache",
        "Referer" to "https://x.com",
        "X-Twitter-Active-User" to "yes",
        "X-Twitter-Client-Language" to "en"
    )

    @Throws(IOException::class)
    fun fetchUrl(url: String): String {
        var currentUrl = url
        var redirects = 0

        while (redirects < 5) {
            val conn = (URL(currentUrl).openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = false
                defaultHeaders.forEach { (k, v) -> setRequestProperty(k, v) }
            }

            val code = conn.responseCode
            if (code in 300..399) {
                currentUrl = conn.getHeaderField("Location")
                    ?: throw IOException("Redirect with no Location header from $currentUrl")
                conn.disconnect()
                redirects++
                continue
            }

            if (code !in 200..299) {
                conn.disconnect()
                throw IOException("HTTP $code from $currentUrl")
            }

            val body = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
            conn.disconnect()
            return body
        }

        throw IOException("Too many redirects for $url")
    }

}
