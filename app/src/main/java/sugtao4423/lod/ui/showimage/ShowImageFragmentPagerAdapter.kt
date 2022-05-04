package sugtao4423.lod.ui.showimage

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import sugtao4423.lod.ui.showimage.fragment.ShowImageFragment

class ShowImageFragmentPagerAdapter(fm: FragmentManager, imageUrls: Array<String>) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments: List<ShowImageFragment> = imageUrls.map { url ->
        ShowImageFragment().apply {
            arguments = Bundle().also {
                it.putString(ShowImageFragment.BUNDLE_KEY_URL, url)
            }
        }
    }

    override fun getItem(i: Int): Fragment = fragments[i]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence = "${position + 1}/$count"

}
