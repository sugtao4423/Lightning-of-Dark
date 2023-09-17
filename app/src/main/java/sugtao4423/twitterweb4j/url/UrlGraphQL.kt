package sugtao4423.twitterweb4j.url

class UrlGraphQL : UrlBase() {
    companion object {

        @JvmStatic
        val createTweet = "$graphQLApiBaseUrl/PIZtQLRIYtSa9AtW_fI2Mw/CreateTweet"

        @JvmStatic
        val deleteTweet = "$graphQLApiBaseUrl/VaenaVgh5q5ih7kvyVjgtg/DeleteTweet"

        @JvmStatic
        val createRetweet = "$graphQLApiBaseUrl/ojPdsZsimiJrUGLR1sjUtA/CreateRetweet"

        @JvmStatic
        val deleteRetweet = "$graphQLApiBaseUrl/iQtK4dl5hBmXewYZuEOKVw/DeleteRetweet"

        @JvmStatic
        val favoriteTweet = "$graphQLApiBaseUrl/lI07N6Otwv1PhnEgXILM7A/FavoriteTweet"

    }
}
