package sugtao4423.twitterweb4j.url

object UrlGraphQL {

    @JvmStatic
    private val apiBaseUrl = "https://twitter.com/i/api/graphql"

    @JvmStatic
    val createTweet = "$apiBaseUrl/PIZtQLRIYtSa9AtW_fI2Mw/CreateTweet"

    @JvmStatic
    val deleteTweet = "$apiBaseUrl/VaenaVgh5q5ih7kvyVjgtg/DeleteTweet"

    @JvmStatic
    val createRetweet = "$apiBaseUrl/ojPdsZsimiJrUGLR1sjUtA/CreateRetweet"

    @JvmStatic
    val deleteRetweet = "$apiBaseUrl/iQtK4dl5hBmXewYZuEOKVw/DeleteRetweet"

    @JvmStatic
    val favoriteTweet = "$apiBaseUrl/lI07N6Otwv1PhnEgXILM7A/FavoriteTweet"

    @JvmStatic
    val unfavoriteTweet = "$apiBaseUrl/ZYKSe-w7KEslx3JhSIk5LA/UnfavoriteTweet"

}
