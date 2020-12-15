package sugtao4423.lod.userpage_fragment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import sugtao4423.lod.R
import twitter4j.User

class UserPageFragmentPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val detail = _0_detail()
    private val tweet = _1_Tweet()
    private val favorites = _2_favorites()
    private val follow = _3_follow()
    private val follower = _4_follower()

    override fun getItem(i: Int): Fragment {
        return when (i) {
            1 -> tweet
            2 -> favorites
            3 -> follow
            4 -> follower
            else -> detail
        }
    }

    override fun getCount(): Int {
        return 5
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            1 -> context.getString(R.string.page_title_tweet)
            2 -> context.getString(R.string.page_title_favorite)
            3 -> context.getString(R.string.page_title_follow)
            4 -> context.getString(R.string.page_title_follower)
            else -> context.getString(R.string.page_title_detail)
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