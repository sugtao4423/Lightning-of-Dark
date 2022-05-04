package sugtao4423.lod.ui.userpage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import sugtao4423.lod.R
import sugtao4423.lod.ui.userpage.fragment.DetailFragment
import sugtao4423.lod.ui.userpage.fragment.StatusFragment
import sugtao4423.lod.ui.userpage.fragment.UserFragment

class UserPageFragmentPagerAdapter(fm: FragmentManager, private val context: Context) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(i: Int): Fragment {
        return when (i) {
            1 -> StatusFragment().also {
                it.arguments = Bundle().apply {
                    putString(StatusFragment.KEY_FRAGMENT_TYPE, StatusFragment.TYPE_TWEET)
                }
            }
            2 -> StatusFragment().also {
                it.arguments = Bundle().apply {
                    putString(StatusFragment.KEY_FRAGMENT_TYPE, StatusFragment.TYPE_FAVORITE)
                }
            }
            3 -> UserFragment().also {
                it.arguments = Bundle().apply {
                    putString(UserFragment.KEY_FRAGMENT_TYPE, UserFragment.TYPE_FOLLOW)
                }
            }
            4 -> UserFragment().also {
                it.arguments = Bundle().apply {
                    putString(UserFragment.KEY_FRAGMENT_TYPE, UserFragment.TYPE_FOLLOWER)
                }
            }
            else -> DetailFragment()
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
