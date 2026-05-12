package sugtao4423.lod.ui.userpage.converter

import sugtao4423.twitter4j.UrlEntity
import sugtao4423.twitter4j.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object UserConverter {

    private const val NULL = "null"
    private const val LOADING = "Loading…"

    @JvmStatic
    fun iconUrl(user: User?): String? = user?.profileImage?.originalUrl

    @JvmStatic
    fun bannerUrl(user: User?): String? = user?.profileBanner?.retinaUrl

    @JvmStatic
    fun name(user: User?): String = user?.name ?: NULL

    @JvmStatic
    fun screenName(user: User?): String = user?.let { "@${it.screenName}" } ?: NULL

    @JvmStatic
    fun isShowProtected(user: User?): Boolean = user?.isProtected == true

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
            result = replaceUrlEntities(target, it)
        }
        return result
    }

    @JvmStatic
    fun bio(user: User?): String = user?.let {
        replaceUrlEntities(it.description, it.descriptionUrlEntities)
    } ?: LOADING

    @JvmStatic
    fun location(user: User?): String = user?.location ?: LOADING

    @JvmStatic
    fun link(user: User?): String = user?.let {
        replaceUrlEntities(it.url, it.urlEntity)
    } ?: LOADING

    @JvmStatic
    fun tweetCount(user: User?): String = user?.let {
        NumberFormat.getInstance().format(it.statusesCount)
    } ?: LOADING

    @JvmStatic
    fun favoriteCount(user: User?): String = user?.let {
        NumberFormat.getInstance().format(it.favouritesCount)
    } ?: LOADING

    @JvmStatic
    fun followCount(user: User?): String = user?.let {
        NumberFormat.getInstance().format(it.friendsCount)
    } ?: LOADING

    @JvmStatic
    fun followerCount(user: User?): String = user?.let {
        NumberFormat.getInstance().format(it.followersCount)
    } ?: LOADING

    @JvmStatic
    fun createDate(user: User?): String = user?.let {
        SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(it.createdAt)
    } ?: LOADING

}
