package sugtao4423.twitterweb4j.parser

import sugtao4423.twitterweb4j.parseJson
import twitter4j.Relationship
import twitter4j.TwitterException
import twitter4j.TwitterObjectFactory
import twitter4j.User
import twitter4j.UserList

object JsonParserV1 {

    @Throws(TwitterException::class)
    fun parseUserListsArray(response: String): List<UserList> = runCatching {
        val json = response.parseJson()
        List(json.size) {
            TwitterObjectFactory.createUserList(json[it].toString())
        }
    }.getOrElse {
        throw TwitterException(it.message, it.cause)
    }

    @Throws(TwitterException::class)
    fun parseUser(response: String): User {
        return TwitterObjectFactory.createUser(response)
    }

    @Throws(TwitterException::class)
    fun parseRelationship(response: String): Relationship {
        return TwitterObjectFactory.createRelationship(response)
    }

}
