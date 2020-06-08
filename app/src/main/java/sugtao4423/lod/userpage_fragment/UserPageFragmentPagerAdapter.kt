package sugtao4423.lod.userpage_fragment

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import sugtao4423.lod.R
import twitter4j.User

class UserPageFragmentPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentStatePagerAdapter(fm) {

    private val detail = _0_detail()
    private val tweet = _1_Tweet()
    private val favorites = _2_favorites()
    private val follow = _3_follow()
    private val follower = _4_follower()

    override fun getItem(i: Int): Fragment? {
        return when (i) {
            0 -> detail
            1 -> tweet
            2 -> favorites
            3 -> follow
            4 -> follower
            else -> null
        }
    }

    override fun getCount(): Int {
        return 5
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.page_title_detail)
            1 -> context.getString(R.string.page_title_tweet)
            2 -> context.getString(R.string.page_title_favorite)
            3 -> context.getString(R.string.page_title_follow)
            4 -> context.getString(R.string.page_title_follower)
            else -> null
        }
    }

    fun setTargetUser(user: User) {
        detail.targetUser = user
        detail.setText()
        tweet.targetUser = user
        favorites.targetUser = user
        follow.targetUser = user
        follower.targetUser = user
    }

}