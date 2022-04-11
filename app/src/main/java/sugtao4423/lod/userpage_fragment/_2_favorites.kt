package sugtao4423.lod.userpage_fragment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.lod.R
import sugtao4423.lod.ShowToast
import twitter4j.Paging
import twitter4j.TwitterException

class _2_favorites : UserPageListBaseFragment(FragmentType.TYPE_TWEET) {

    override fun loadList() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                val paging = Paging(1, 50).let {
                    if (tweetListAdapter.itemCount > 0) it.maxId(tweetListAdapter.data.last().id - 1) else it
                }

                try {
                    app.getTwitter().getFavorites(targetUser!!.screenName, paging)
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result != null) {
                tweetListAdapter.addAll(result)
                if (targetUser != null && result.isEmpty()) {
                    isAllLoaded = true
                }
            } else {
                ShowToast(requireContext().applicationContext, R.string.error_get_favorite)
            }
            binding.userPagePull.isRefreshing = false
            binding.userPagePull.isEnabled = true
        }
    }

}
