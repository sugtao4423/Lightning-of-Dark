package sugtao4423.twitter4j

data class Relationship(
    val sourceUserId: Long,
    val targetUserId: Long,
    val sourceUserScreenName: String,
    val targetUserScreenName: String,
    val isSourceBlockedByTarget: Boolean,
    val isSourceBlockingTarget: Boolean,
    val isSourceFollowingTarget: Boolean,
    val isSourceFollowedByTarget: Boolean,
    val isSourceMutingTarget: Boolean,
    val canSourceDm: Boolean,
    val isSourceNotificationsEnabled: Boolean,
    val isSourceWantRetweets: Boolean,
) : java.io.Serializable
