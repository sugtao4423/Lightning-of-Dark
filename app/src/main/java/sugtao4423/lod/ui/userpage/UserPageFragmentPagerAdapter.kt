package sugtao4423.lod.ui.userpage

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import sugtao4423.lod.R
import sugtao4423.lod.ui.userpage.fragment.*

class UserPageFragmentPagerAdapter(fm: FragmentManager, private val context: Context) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(i: Int): Fragment {
        return when (i) {
            1 -> _1_Tweet()
            2 -> _2_favorites()
            3 -> _3_follow()
            4 -> _4_follower()
            else -> _0_detail()
        }
    }

    override fun getCount(): Int = 5

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            1 -> context.getString(R.string.page_title_tweet)
            2 -> context.getString(R.string.page_title_favorite)
            3 -> context.getString(R.string.page_title_follow)
            4 -> context.getString(R.string.page_title_follower)
            else -> context.getString(R.string.page_title_detail)
        }
    }

}
