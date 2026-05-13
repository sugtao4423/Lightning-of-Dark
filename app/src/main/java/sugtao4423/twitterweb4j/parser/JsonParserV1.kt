package sugtao4423.twitterweb4j.parser

import sugtao4423.twitter4j.TwitterException
import sugtao4423.twitter4j.User
import sugtao4423.twitter4j.UserList
import sugtao4423.twitterweb4j.parseJson
import sugtao4423.twitterweb4j.parser.model.parseUserListV1
import sugtao4423.twitterweb4j.parser.model.parseUserV1
import twitter4j.Relationship
import twitter4j.TwitterObjectFactory

object JsonParserV1 {

    @Throws(TwitterException::class)
    fun parseUserListsArray(response: String): List<UserList> = runCatching {
        val json = response.parseJson()
        List(json.size) { parseUserListV1(json[it]) }
    }.getOrElse {
        throw TwitterException(it.message, it.cause)
    }

    @Throws(TwitterException::class)
    fun parseUser(response: String): User = runCatching {
        parseUserV1(response.parseJson())
    }.getOrElse {
        throw TwitterException(it.message, it.cause)
    }

    @Throws(TwitterException::class)
    fun parseRelationship(response: String): Relationship {
        return TwitterObjectFactory.createRelationship(response)
    }

}
