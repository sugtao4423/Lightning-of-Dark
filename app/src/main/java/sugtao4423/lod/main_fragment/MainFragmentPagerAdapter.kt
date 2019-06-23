package sugtao4423.lod.main_fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import sugtao4423.lod.App
import sugtao4423.lod.R

class MainFragmentPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentStatePagerAdapter(fm) {

    private val app = context.applicationContext as App
    val fragmentMention = Fragment_mention()
    val fragmentHome = Fragment_home()

    override fun getItem(i: Int): Fragment {
        return when (i) {
            0 -> fragmentMention
            1 -> fragmentHome
            else -> {
                val list = Fragment_List()
                val args = Bundle()
                args.putInt(Fragment_List.LIST_INDEX, i - 2)
                list.arguments = args
                return list
            }
        }
    }

    override fun getCount(): Int {
        return app.getLists(context).size + 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val lists = app.getLists(context)
        return when (position) {
            0 -> context.getString(R.string.page_title_mention)
            1 -> context.getString(R.string.param_page_title_home, app.getLevel().getLevel())
            else -> lists[position - 2].listName
        }
    }

}