package sugtao4423.lod.ui.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import sugtao4423.lod.App
import sugtao4423.lod.R
import sugtao4423.lod.ui.main.fragment.HomeFragment
import sugtao4423.lod.ui.main.fragment.ListFragment
import sugtao4423.lod.ui.main.fragment.MentionFragment

class MainFragmentPagerAdapter(
    fm: FragmentManager,
    private val context: Context,
    private val listData: List<MainActivityViewModel.ListData>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(i: Int): Fragment {
        return when (i) {
            0 -> MentionFragment()
            1 -> HomeFragment()
            else -> ListFragment().apply {
                arguments = Bundle().also {
                    it.putInt(ListFragment.LIST_INDEX, i - 2)
                }
            }
        }
    }

    override fun getCount(): Int = listData.size + 2

    override fun getPageTitle(position: Int): CharSequence {
        val level = (context.applicationContext as App).levelRepository.getLevel()
        return when (position) {
            0 -> context.getString(R.string.page_title_mention)
            1 -> context.getString(R.string.param_page_title_home, level)
            else -> listData[position - 2].name
        }
    }

}
