package sugtao4423.lod.ui.userpage.converter

import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object UserConverter {

    @JvmStatic
    private val numberFormat = NumberFormat.getInstance()

    @JvmStatic
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE)

    @JvmStatic
    fun iconUrl(user: User): String? = user.profileImage?.originalUrl

    @JvmStatic
    fun bannerUrl(user: User): String? = user.profileBanner?.retinaUrl

    @JvmStatic
    fun name(user: User): String = user.name

    @JvmStatic
    fun screenName(user: User): String = "@${user.screenName}"

    @JvmStatic
    fun isShowProtected(user: User): Boolean = user.isProtected

    @JvmStatic
    private fun replaceUrlEntities(target: String?, entity: UrlEntity?): String = when {
        target == null -> ""
        entity == null || entity.expandedUrl == null -> target
        else -> target.replace(entity.url, entity.expandedUrl)
    }

    @JvmStatic
    private fun replaceUrlEntities(target: String?, entity: List<UrlEntity>): String {
        var result = target ?: ""
        entity.forEach {
            result = replaceUrlEntities(result, it)
        }
        return result
    }

    @JvmStatic
    fun bio(user: User): String = replaceUrlEntities(user.description, user.descriptionUrlEntities)

    @JvmStatic
    fun location(user: User): String = user.location ?: ""

    @JvmStatic
    fun link(user: User): String = replaceUrlEntities(user.url, user.urlEntity)

    @JvmStatic
    fun tweetCount(user: User): String = numberFormat.format(user.statusesCount)

    @JvmStatic
    fun favoriteCount(user: User): String = numberFormat.format(user.favouritesCount)

    @JvmStatic
    fun followCount(user: User): String = numberFormat.format(user.friendsCount)

    @JvmStatic
    fun followerCount(user: User): String = numberFormat.format(user.followersCount)

    @JvmStatic
    fun createDate(user: User): String = dateFormat.format(user.createdAt)

}
