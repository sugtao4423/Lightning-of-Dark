package sugtao4423.twitterweb4j.parser.model

import org.json.JSONException
import sugtao4423.twitter4j.Relationship
import sugtao4423.twitterweb4j.Json

@Throws(JSONException::class)
fun parseRelationshipV1(json: Json): Relationship {
    val source = json["relationship"]["source"]
    val target = json["relationship"]["target"]

    val sourceUserId = source["id_str"].string.toLong()
    val targetUserId = target["id_str"].string.toLong()
    val sourceUserScreenName = source["screen_name"].string
    val targetUserScreenName = target["screen_name"].string

    val isSourceBlockedByTarget = source["blocked_by"].bool
    val isSourceBlockingTarget = source["blocking"].bool
    val isSourceFollowingTarget = source["following"].bool
    val isSourceFollowedByTarget = source["followed_by"].bool
    val isSourceMutingTarget = source["muting"].bool
    val canSourceDm = source["can_dm"].bool
    val isSourceNotificationsEnabled = source["notifications_enabled"].bool
    val isSourceWantRetweets = source["want_retweets"].bool

    return Relationship(
        sourceUserId,
        targetUserId,
        sourceUserScreenName,
        targetUserScreenName,
        isSourceBlockedByTarget,
        isSourceBlockingTarget,
        isSourceFollowingTarget,
        isSourceFollowedByTarget,
        isSourceMutingTarget,
        canSourceDm,
        isSourceNotificationsEnabled,
        isSourceWantRetweets,
    )
}
