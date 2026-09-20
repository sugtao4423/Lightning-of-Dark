package sugtao4423.twitter4j

import java.util.Date

data class UserList(
    val id: Long,
    val name: String,
    val description: String,
    val subscriberCount: Int,
    val memberCount: Int,
    val isPublic: Boolean,
    val isFollowing: Boolean,
    val user: User,
    val createdAt: Date,
) : Comparable<UserList>, java.io.Serializable {

    override fun compareTo(other: UserList): Int = id.compareTo(other.id)

}
