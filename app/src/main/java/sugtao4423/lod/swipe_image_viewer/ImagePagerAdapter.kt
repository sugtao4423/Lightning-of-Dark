package sugtao4423.lod.swipe_image_viewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ImagePagerAdapter(fm: FragmentManager, private val urls: Array<String>) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        const val BUNDLE_KEY_URL = "url"
    }

    private val fragments: Array<ImageFragment>

    init {
        val fragments = arrayOfNulls<ImageFragment>(urls.size)
        for (i in urls.indices) {
            val bundle = Bundle().apply {
                putString(BUNDLE_KEY_URL, urls[i])
            }
            fragments[i] = ImageFragment()
            fragments[i]!!.arguments = bundle
        }
        this.fragments = fragments.requireNoNulls()
    }

    override fun getItem(i: Int): Fragment {
        return fragments[i]
    }

    override fun getCount(): Int {
        return urls.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return "${position + 1}/$count"
    }

}