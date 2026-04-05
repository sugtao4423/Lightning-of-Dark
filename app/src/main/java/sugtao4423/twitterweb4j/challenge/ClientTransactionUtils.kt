package sugtao4423.twitterweb4j.challenge

object ClientTransactionUtils {

    private val ON_DEMAND_FILE_REGEX = Regex(""",(\d+):["']ondemand\.s["']""")
    private val ON_DEMAND_HASH_PATTERN = """,%s:"([0-9a-f]+)""""

    val homePageUrl = "https://x.com"

    @Throws(IllegalStateException::class)
    fun getOndemandFileUrl(homePageHtml: String): String {
        val fileMatch = ON_DEMAND_FILE_REGEX.find(homePageHtml)
            ?: throw IllegalStateException("Could not find \"ondemand.s\" reference in homepage HTML. The page structure may have changed.")
        val fileNum = fileMatch.groupValues[1]

        val fileHashMatch = ON_DEMAND_HASH_PATTERN.format(fileNum).toRegex().find(homePageHtml)
            ?: throw IllegalStateException("Could not find hash for \"ondemand.s\" file number $fileNum in homepage HTML. The page structure may have changed.")
        val hash = fileHashMatch.groupValues[1]
        return "https://abs.twimg.com/responsive-web/client-web/ondemand.s.${hash}a.js"
    }

}
