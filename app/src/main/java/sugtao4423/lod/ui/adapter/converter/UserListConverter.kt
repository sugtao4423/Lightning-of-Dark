package sugtao4423.lod.ui.adapter.converter

import sugtao4423.twitter4j.User
import java.text.NumberFormat

object UserListConverter {

    private val numberFormat = NumberFormat.getInstance()

    fun isShowProtected(user: User): Boolean = user.isProtected

    fun userIconUrl(user: User): String? = user.profileImage?.biggerUrl

    fun userNameAndScreenName(user: User): String = user.let {
        "${it.name} - @${it.screenName}"
    }

    fun userDescription(user: User): String? = user.description

    fun userCountDetail(user: User): String = String.format(
        "Tweet: %s  Fav: %s  Follow: %s  Follower: %s",
        numberFormat.format(user.statusesCount),
        numberFormat.format(user.favouritesCount),
        numberFormat.format(user.friendsCount),
        numberFormat.format(user.followersCount)
    )

}
