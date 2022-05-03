package sugtao4423.lod.ui.adapter.converter

import twitter4j.User
import java.text.NumberFormat

object UserViewDataConverter {

    @JvmStatic
    fun isShowProtected(user: User?): Boolean = user?.isProtected ?: false

    @JvmStatic
    fun userIconUrl(user: User?): String? = user?.biggerProfileImageURLHttps

    @JvmStatic
    fun userNameAndScreenName(user: User?): String? = user?.let {
        "${it.name} - @${it.screenName}"
    }

    @JvmStatic
    fun userDescription(user: User?): String? = user?.description

    @JvmStatic
    fun userCountDetail(user: User?): String? = user?.let {
        fun num(n: Int): String = NumberFormat.getInstance().format(n)
        String.format(
            "Tweet: %s  Fav: %s  Follow: %s  Follower: %s",
            num(it.statusesCount),
            num(it.favouritesCount),
            num(it.friendsCount),
            num(it.followersCount)
        )
    }

}
