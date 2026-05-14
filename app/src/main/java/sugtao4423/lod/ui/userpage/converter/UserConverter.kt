package sugtao4423.lod.ui.userpage.converter

import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object UserConverter {

    private val numberFormat = NumberFormat.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE)

    fun iconUrl(user: User): String? = user.profileImage?.originalUrl
    fun bannerUrl(user: User): String? = user.profileBanner?.retinaUrl

    fun name(user: User): String = user.name
    fun screenName(user: User): String = "@${user.screenName}"
    fun isShowProtected(user: User): Boolean = user.isProtected

    private fun replaceUrlEntities(target: String?, entity: UrlEntity?): String = when {
        target == null -> ""
        entity == null || entity.expandedUrl == null -> target
        else -> target.replace(entity.url, entity.expandedUrl)
    }

    private fun replaceUrlEntities(target: String?, entity: List<UrlEntity>): String {
        var result = target ?: ""
        entity.forEach {
            result = replaceUrlEntities(result, it)
        }
        return result
    }

    fun bio(user: User): String = replaceUrlEntities(user.description, user.descriptionUrlEntities)
    fun location(user: User): String = user.location ?: ""
    fun link(user: User): String = replaceUrlEntities(user.url, user.urlEntity)

    fun tweetCount(user: User): String = numberFormat.format(user.statusesCount)
    fun favoriteCount(user: User): String = numberFormat.format(user.favouritesCount)
    fun followCount(user: User): String = numberFormat.format(user.friendsCount)
    fun followerCount(user: User): String = numberFormat.format(user.followersCount)
    fun createDate(user: User): String = dateFormat.format(user.createdAt)

}
