package sugtao4423.twitterweb4j.parser

import sugtao4423.twitter4j.Relationship
import sugtao4423.twitter4j.TwitterException
import sugtao4423.twitter4j.User
import sugtao4423.twitter4j.UserList
import sugtao4423.twitterweb4j.Json
import sugtao4423.twitterweb4j.parseJson
import sugtao4423.twitterweb4j.parser.model.parseRelationshipV1
import sugtao4423.twitterweb4j.parser.model.parseUserListV1
import sugtao4423.twitterweb4j.parser.model.parseUserV1

object JsonParserV1 {

    @Throws(TwitterException::class)
    private inline fun <T> parse(response: String, transform: (Json) -> T): T = runCatching {
        transform(response.parseJson())
    }.getOrElse { throw TwitterException(it.message, it.cause) }

    @Throws(TwitterException::class)
    fun parseUserListsArray(response: String): List<UserList> = parse(response) { json ->
        List(json.size) { parseUserListV1(json[it]) }
    }

    @Throws(TwitterException::class)
    fun parseUser(response: String): User = parse(response) {
        parseUserV1(it)
    }

    @Throws(TwitterException::class)
    fun parseRelationship(response: String): Relationship = parse(response) {
        parseRelationshipV1(it)
    }

}
