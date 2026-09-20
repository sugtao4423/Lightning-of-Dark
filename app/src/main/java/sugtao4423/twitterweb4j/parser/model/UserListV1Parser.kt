package sugtao4423.twitterweb4j.parser.model

import org.json.JSONException
import sugtao4423.twitter4j.UserList
import sugtao4423.twitterweb4j.Json

@Throws(JSONException::class)
fun parseUserListV1(json: Json): UserList {
    val id = json["id_str"].string.toLong()
    val name = json["name"].string
    val description = json["description"].string
    val subscriberCount = json["subscriber_count"].int
    val memberCount = json["member_count"].int
    val isPublic = json["mode"].string.lowercase() == "public"
    val isFollowing = json["following"].bool
    val user = parseUserV1(json["user"])
    val createdAt = parseTwitterDate(json["created_at"].string)

    return UserList(
        id,
        name,
        description,
        subscriberCount,
        memberCount,
        isPublic,
        isFollowing,
        user,
        createdAt,
    )
}
