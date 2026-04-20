package sugtao4423.twitterweb4j.parser

import twitter4j.JSONArray
import twitter4j.Relationship
import twitter4j.TwitterException
import twitter4j.TwitterObjectFactory
import twitter4j.User
import twitter4j.UserList

object JsonParserV1 {

    @Throws(TwitterException::class)
    fun parseUserListsArray(response: String): List<UserList> {
        val json = runCatching { JSONArray(response) }.getOrElse {
            throw TwitterException(it.message, it.cause)
        }

        val userLists = mutableListOf<UserList>()
        for (i in 0 until json.length()) {
            val userListObject = json.getJSONObject(i)
            val userList = TwitterObjectFactory.createUserList(userListObject.toString())
            userLists.add(userList)
        }
        return userLists.toList()
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
