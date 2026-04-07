package sugtao4423.twitterweb4j.parser

import twitter4j.Relationship
import twitter4j.TwitterException
import twitter4j.TwitterObjectFactory
import twitter4j.User

object JsonParserV1 {

    @Throws(TwitterException::class)
    fun parseUser(response: String): User {
        return TwitterObjectFactory.createUser(response)
    }

    @Throws(TwitterException::class)
    fun parseRelationship(response: String): Relationship {
        return TwitterObjectFactory.createRelationship(response)
    }

}
